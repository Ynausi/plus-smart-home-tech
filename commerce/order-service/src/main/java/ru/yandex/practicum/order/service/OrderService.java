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
import ru.yandex.practicum.order.exception.NotFoundException;
import ru.yandex.practicum.order.exception.OrderProcessingException;
import ru.yandex.practicum.order.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository repository;
    private final OrderPersistenceService persistenceService;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;

    public OrderDto create(CreateOrderRequest request) {
        Map<Long, ProductClientResponse> products = loadProductsOnce(request.items());
        Map<Long, Integer> quantitiesByProduct = aggregateQuantities(request.items());
        List<InventoryOperationRequest> successfulReservations = new ArrayList<>();

        try {
            for (Map.Entry<Long, Integer> entry : quantitiesByProduct.entrySet()) {
                InventoryOperationRequest reservation =
                        new InventoryOperationRequest(entry.getKey(), entry.getValue());

                reserve(reservation);
                successfulReservations.add(reservation);
            }

            OrderEntity order = buildConfirmedOrder(request, products);
            return toDto(persistenceService.save(order));
        } catch (OrderProcessingException e) {
            compensate(successfulReservations);
            throw e;
        } catch (RuntimeException e) {
            compensate(successfulReservations);
            log.error("Не удалось завершить создание заказа", e);
            throw new OrderProcessingException("Не удалось завершить создание заказа", e);
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

    private Map<Long, ProductClientResponse> loadProductsOnce(List<OrderItemRequest> items) {
        Map<Long, ProductClientResponse> products = new LinkedHashMap<>();

        for (OrderItemRequest item : items) {
            if (products.containsKey(item.productId())) {
                continue;
            }

            ProductClientResponse product = loadProduct(item.productId());
            if (!Boolean.TRUE.equals(product.active())) {
                throw new OrderProcessingException(
                        "Товар с id=" + item.productId() + " снят с продажи"
                );
            }

            products.put(item.productId(), product);
        }

        return products;
    }

    private ProductClientResponse loadProduct(Long productId) {
        try {
            ProductClientResponse product = productClient.findById(productId);
            if (product == null || product.id() == null) {
                throw new OrderProcessingException("Товар с id=" + productId + " не найден");
            }
            return product;
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new OrderProcessingException("Товар с id=" + productId + " не найден");
            }

            throw new OrderProcessingException(
                    "Не удалось получить данные товара с id=" + productId,
                    e
            );
        } catch (OrderProcessingException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new OrderProcessingException(
                    "Не удалось получить данные товара с id=" + productId,
                    e
            );
        }
    }

    private Map<Long, Integer> aggregateQuantities(List<OrderItemRequest> items) {
        Map<Long, Integer> quantities = new LinkedHashMap<>();

        for (OrderItemRequest item : items) {
            quantities.merge(item.productId(), item.quantity(), Math::addExact);
        }

        return quantities;
    }

    private void reserve(InventoryOperationRequest request) {
        try {
            InventoryOperationResponse response = inventoryClient.reserve(request);

            if (response == null || !response.success()) {
                throw new OrderProcessingException(
                        "Не удалось зарезервировать товар с id=" + request.productId()
                );
            }
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new OrderProcessingException(
                        "Складская запись для товара с id=" + request.productId() + " не найдена"
                );
            }

            if (e.status() == 409) {
                throw new OrderProcessingException(
                        "Недостаточно товара с id=" + request.productId() + " для создания заказа"
                );
            }

            throw new OrderProcessingException(
                    "Не удалось зарезервировать товар с id=" + request.productId(),
                    e
            );
        } catch (OrderProcessingException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new OrderProcessingException(
                    "Не удалось зарезервировать товар с id=" + request.productId(),
                    e
            );
        }
    }

    private void compensate(List<InventoryOperationRequest> successfulReservations) {
        ListIterator<InventoryOperationRequest> iterator =
                successfulReservations.listIterator(successfulReservations.size());

        while (iterator.hasPrevious()) {
            InventoryOperationRequest reservation = iterator.previous();
            try {
                inventoryClient.release(reservation);
            } catch (RuntimeException e) {
                log.error(
                        "Не удалось снять резерв для productId={}, quantity={}",
                        reservation.productId(),
                        reservation.quantity(),
                        e
                );
            }
        }
    }

    private OrderEntity buildConfirmedOrder(
            CreateOrderRequest request,
            Map<Long, ProductClientResponse> products
    ) {
        OrderEntity order = new OrderEntity();
        order.setCustomerName(request.customerName());
        order.setCustomerEmail(request.customerEmail());
        order.setStatus(OrderStatus.CONFIRMED);
        order.setCreatedAt(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest requestedItem : request.items()) {
            ProductClientResponse product = products.get(requestedItem.productId());

            if (product == null || product.name() == null || product.price() == null) {
                throw new OrderProcessingException(
                        "Не удалось получить полные данные товара с id=" + requestedItem.productId()
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
}
