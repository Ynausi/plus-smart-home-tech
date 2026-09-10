package ru.yandex.practicum.order.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.order.client.InventoryClient;
import ru.yandex.practicum.order.client.ProductClient;
import ru.yandex.practicum.order.client.dto.InventoryOperationRequest;
import ru.yandex.practicum.order.client.dto.InventoryOperationResponse;
import ru.yandex.practicum.order.client.dto.ProductClientResponse;
import ru.yandex.practicum.order.dto.CreateOrderRequest;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.order.dto.OrderItemDto;
import ru.yandex.practicum.order.dto.OrderItemRequest;
import ru.yandex.practicum.order.entity.OrderEntity;
import ru.yandex.practicum.order.entity.OrderItem;
import ru.yandex.practicum.order.entity.OrderStatus;
import ru.yandex.practicum.order.exception.InventoryServiceUnavailableException;
import ru.yandex.practicum.order.exception.NotFoundException;
import ru.yandex.practicum.order.exception.OrderProcessingException;
import ru.yandex.practicum.order.exception.ProductServiceUnavailableException;
import ru.yandex.practicum.order.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository repository;
    private final OrderPersistenceService persistenceService;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;

    public OrderDto create(CreateOrderRequest request) {
        Map<Long, Integer> quantitiesByProduct = aggregateQuantities(request.items());
        Map<Long, ProductSnapshot> products = new LinkedHashMap<>();
        Set<String> degradationReasons = new LinkedHashSet<>();

        for (Long productId : quantitiesByProduct.keySet()) {
            ProductLoadResult result = loadProduct(productId);

            if (result.degraded()) {
                degradationReasons.add(result.reason());
                products.put(
                        productId,
                        new ProductSnapshot(
                                productId,
                                "Товар #" + productId + " (ожидает проверки)",
                                BigDecimal.ZERO
                        )
                );
                continue;
            }

            ProductClientResponse product = result.product();
            if (!Boolean.TRUE.equals(product.active())) {
                throw new OrderProcessingException(
                        "Товар с id=" + productId + " снят с продажи"
                );
            }

            if (product.id() == null || product.name() == null || product.price() == null) {
                throw new OrderProcessingException(
                        "product-service вернул неполные данные для товара с id=" + productId
                );
            }

            products.put(
                    productId,
                    new ProductSnapshot(product.id(), product.name(), product.price())
            );
        }

        List<InventoryOperationRequest> successfulReservations = new ArrayList<>();

        try {
            for (Map.Entry<Long, Integer> entry : quantitiesByProduct.entrySet()) {
                InventoryOperationRequest reservation =
                        new InventoryOperationRequest(entry.getKey(), entry.getValue());

                InventoryReserveResult result = reserve(reservation);

                if (result.degraded()) {
                    degradationReasons.add(result.reason());
                    continue;
                }

                successfulReservations.add(reservation);
            }

            boolean degraded = !degradationReasons.isEmpty();
            OrderEntity order = buildOrder(request, products, degraded, degradationReasons);
            return toDto(persistenceService.save(order));
        } catch (RuntimeException e) {
            compensate(successfulReservations);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public OrderDto findById(Long id) {
        return toDto(repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Заказ с id=" + id + " не найден")));
    }

    @Transactional(readOnly = true)
    public List<OrderDto> findAll() {
        return repository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderDto> findByEmail(String email) {
        return repository.findAllByCustomerEmailIgnoreCaseOrderByCreatedAtDesc(email).stream()
                .map(this::toDto)
                .toList();
    }

    private ProductLoadResult loadProduct(Long productId) {
        try {
            ProductClientResponse product = productClient.findById(productId);

            if (product == null) {
                return ProductLoadResult.degraded(
                        "product-service недоступен для productId=" + productId
                );
            }

            return ProductLoadResult.success(product);
        } catch (RuntimeException e) {
            OrderProcessingException businessFailure = findCause(e, OrderProcessingException.class);
            if (businessFailure != null) {
                throw businessFailure;
            }

            ProductServiceUnavailableException unavailable =
                    findCause(e, ProductServiceUnavailableException.class);
            if (unavailable != null) {
                log.warn("Техническая деградация product-service для productId={}", productId, unavailable);
                return ProductLoadResult.degraded(
                        "product-service недоступен для productId=" + productId
                );
            }

            FeignException feignException = findCause(e, FeignException.class);
            if (feignException != null) {
                if (feignException.status() >= 400 && feignException.status() < 500) {
                    throw new OrderProcessingException(
                            "product-service отклонил запрос, HTTP " + feignException.status(),
                            e
                    );
                }

                log.warn("Техническая ошибка product-service для productId={}", productId, e);
                return ProductLoadResult.degraded(
                        "product-service недоступен для productId=" + productId
                );
            }

            throw e;
        }
    }

    private InventoryReserveResult reserve(InventoryOperationRequest request) {
        try {
            InventoryOperationResponse response = inventoryClient.reserve(request);

            if (response == null) {
                return InventoryReserveResult.degraded(
                        "inventory-service недоступен для productId=" + request.productId()
                );
            }

            if (!response.success()) {
                throw new OrderProcessingException(
                        "inventory-service не подтвердил резерв для товара с id=" + request.productId()
                );
            }

            return InventoryReserveResult.success();
        } catch (RuntimeException e) {
            OrderProcessingException businessFailure = findCause(e, OrderProcessingException.class);
            if (businessFailure != null) {
                throw businessFailure;
            }

            InventoryServiceUnavailableException unavailable =
                    findCause(e, InventoryServiceUnavailableException.class);
            if (unavailable != null) {
                log.warn(
                        "Техническая деградация inventory-service при резервировании productId={}",
                        request.productId(),
                        unavailable
                );
                return InventoryReserveResult.degraded(
                        "inventory-service недоступен для productId=" + request.productId()
                );
            }

            FeignException feignException = findCause(e, FeignException.class);
            if (feignException != null) {
                if (feignException.status() >= 400 && feignException.status() < 500) {
                    throw new OrderProcessingException(
                            "inventory-service отклонил запрос, HTTP " + feignException.status(),
                            e
                    );
                }

                log.warn(
                        "Техническая ошибка inventory-service при резервировании productId={}",
                        request.productId(),
                        e
                );
                return InventoryReserveResult.degraded(
                        "inventory-service недоступен для productId=" + request.productId()
                );
            }

            throw e;
        }
    }

    private Map<Long, Integer> aggregateQuantities(List<OrderItemRequest> items) {
        Map<Long, Integer> quantities = new LinkedHashMap<>();

        for (OrderItemRequest item : items) {
            try {
                quantities.merge(item.productId(), item.quantity(), Math::addExact);
            } catch (ArithmeticException e) {
                throw new OrderProcessingException(
                        "Слишком большое количество товара с id=" + item.productId(),
                        e
                );
            }
        }

        return quantities;
    }

    private void compensate(List<InventoryOperationRequest> successfulReservations) {
        ListIterator<InventoryOperationRequest> iterator =
                successfulReservations.listIterator(successfulReservations.size());

        while (iterator.hasPrevious()) {
            InventoryOperationRequest reservation = iterator.previous();
            try {
                inventoryClient.release(reservation);
                log.info(
                        "Компенсация выполнена: снят резерв productId={}, quantity={}",
                        reservation.productId(),
                        reservation.quantity()
                );
            } catch (RuntimeException e) {
                log.error(
                        "Не удалось снять резерв для productId={}, quantity={}. Компенсация best-effort",
                        reservation.productId(),
                        reservation.quantity(),
                        e
                );
            }
        }
    }

    private OrderEntity buildOrder(
            CreateOrderRequest request,
            Map<Long, ProductSnapshot> products,
            boolean degraded,
            Set<String> degradationReasons
    ) {
        OrderEntity order = new OrderEntity();
        order.setCustomerName(request.customerName());
        order.setCustomerEmail(request.customerEmail());
        order.setStatus(degraded ? OrderStatus.PENDING_CONFIRMATION : OrderStatus.CONFIRMED);
        order.setCreatedAt(LocalDateTime.now());

        if (degraded) {
            order.setStatusDetails(
                    "Требуется ручная проверка: " + String.join("; ", degradationReasons)
            );
        }

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest requestedItem : request.items()) {
            ProductSnapshot product = products.get(requestedItem.productId());

            if (product == null) {
                throw new OrderProcessingException(
                        "Не удалось подготовить данные товара с id=" + requestedItem.productId()
                );
            }

            OrderItem item = new OrderItem();
            item.setProductId(product.id());
            item.setProductName(product.name());
            item.setQuantity(requestedItem.quantity());
            item.setPrice(product.price());
            order.addItem(item);

            total = total.add(
                    product.price().multiply(BigDecimal.valueOf(requestedItem.quantity()))
            );
        }

        order.setTotalPrice(total);
        return order;
    }

    private OrderDto toDto(OrderEntity order) {
        List<OrderItemDto> items = order.getItems().stream()
                .map(item -> new OrderItemDto(
                        item.getId(),
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .toList();

        return new OrderDto(
                order.getId(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getStatus().name(),
                order.getTotalPrice(),
                order.getStatusDetails(),
                order.getCreatedAt(),
                items
        );
    }

    private static <T extends Throwable> T findCause(Throwable source, Class<T> type) {
        Throwable current = source;
        while (current != null) {
            if (type.isInstance(current)) {
                return type.cast(current);
            }
            current = current.getCause();
        }
        return null;
    }

    private record ProductSnapshot(Long id, String name, BigDecimal price) {
    }

    private record ProductLoadResult(ProductClientResponse product, boolean degraded, String reason) {
        private static ProductLoadResult success(ProductClientResponse product) {
            return new ProductLoadResult(product, false, null);
        }

        private static ProductLoadResult degraded(String reason) {
            return new ProductLoadResult(null, true, reason);
        }
    }

    private record InventoryReserveResult(boolean degraded, String reason) {
        private static InventoryReserveResult success() {
            return new InventoryReserveResult(false, null);
        }

        private static InventoryReserveResult degraded(String reason) {
            return new InventoryReserveResult(true, reason);
        }
    }
}
