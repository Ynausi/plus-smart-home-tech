package ru.yandex.practicum.inventory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.inventory.dto.InventoryDto;
import ru.yandex.practicum.inventory.dto.ReserveRequest;
import ru.yandex.practicum.inventory.dto.ReserveResponse;
import ru.yandex.practicum.inventory.dto.UpdateInventoryRequest;
import ru.yandex.practicum.inventory.entity.Inventory;
import ru.yandex.practicum.inventory.exception.InsufficientStockException;
import ru.yandex.practicum.inventory.exception.InvalidReservationReleaseException;
import ru.yandex.practicum.inventory.exception.InventoryConflictException;
import ru.yandex.practicum.inventory.exception.NotFoundException;
import ru.yandex.practicum.inventory.repository.InventoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {
    private final InventoryRepository repository;

    public List<InventoryDto> findAll() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    public InventoryDto findByProductId(Long id) {
        return toDto(findEntity(id));
    }

    @Transactional
    public InventoryDto create(UpdateInventoryRequest request) {
        if (repository.existsByProductId(request.productId())) {
            throw new InventoryConflictException(
                    "Складская запись для productId=" + request.productId() + " уже существует"
            );
        }

        Inventory inventory = new Inventory();
        inventory.setProductId(request.productId());
        inventory.setQuantity(request.quantity());
        inventory.setReservedQuantity(0);
        return toDto(repository.save(inventory));
    }

    @Transactional
    public InventoryDto update(UpdateInventoryRequest request) {
        Inventory inventory = findEntity(request.productId());

        if (request.quantity() < inventory.getReservedQuantity()) {
            throw new InventoryConflictException(
                    "Общее количество товара не может быть меньше уже зарезервированного: "
                            + inventory.getReservedQuantity()
            );
        }

        inventory.setQuantity(request.quantity());
        return toDto(repository.save(inventory));
    }

    @Transactional
    public ReserveResponse reserve(ReserveRequest request) {
        Inventory inventory = findEntity(request.productId());
        int available = inventory.availableQuantity();

        if (available < request.quantity()) {
            throw new InsufficientStockException(
                    "Недостаточно товара productId=" + request.productId()
                            + ": доступно " + available
                            + ", запрошено " + request.quantity()
            );
        }

        inventory.setReservedQuantity(inventory.getReservedQuantity() + request.quantity());
        Inventory saved = repository.saveAndFlush(inventory);

        return new ReserveResponse(
                true,
                saved.availableQuantity(),
                "Товар успешно зарезервирован"
        );
    }

    @Transactional
    public ReserveResponse release(ReserveRequest request) {
        Inventory inventory = findEntity(request.productId());

        if (inventory.getReservedQuantity() < request.quantity()) {
            throw new InvalidReservationReleaseException(
                    "Нельзя снять резерв productId=" + request.productId()
                            + ": зарезервировано " + inventory.getReservedQuantity()
                            + ", запрошено снять " + request.quantity()
            );
        }

        inventory.setReservedQuantity(inventory.getReservedQuantity() - request.quantity());
        Inventory saved = repository.saveAndFlush(inventory);

        return new ReserveResponse(
                true,
                saved.availableQuantity(),
                "Резерв успешно снят"
        );
    }

    private Inventory findEntity(Long productId) {
        return repository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException(
                        "Складская запись для productId=" + productId + " не найдена"
                ));
    }

    private InventoryDto toDto(Inventory inventory) {
        return new InventoryDto(
                inventory.getId(),
                inventory.getProductId(),
                inventory.getQuantity(),
                inventory.getReservedQuantity(),
                inventory.availableQuantity()
        );
    }
}
