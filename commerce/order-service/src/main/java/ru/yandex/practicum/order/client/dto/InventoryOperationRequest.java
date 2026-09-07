package ru.yandex.practicum.order.client.dto;

public record InventoryOperationRequest(
        Long productId,
        Integer quantity
) {
}
