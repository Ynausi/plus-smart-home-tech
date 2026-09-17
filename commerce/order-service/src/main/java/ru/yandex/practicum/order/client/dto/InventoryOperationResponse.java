package ru.yandex.practicum.order.client.dto;

public record InventoryOperationResponse(
        boolean success,
        Integer availableQuantity,
        String message
) {
}
