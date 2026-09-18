package ru.yandex.practicum.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OrderItemRequest(
        @NotNull(message = "ID товара обязателен")
        Long productId,
        @NotNull(message = "Количество обязательно")
        @Min(value = 1, message = "Количество должно быть не менее 1")
        Integer quantity
) {
    /**
     * Оставлен для совместимости со старыми Java-тестами предыдущего этапа.
     * В HTTP-контракте order-service использует только productId и quantity,
     * а название и цену всегда получает из product-service.
     */
    @Deprecated
    public OrderItemRequest(Long productId, String ignoredProductName, Integer quantity, BigDecimal ignoredPrice) {
        this(productId, quantity);
    }
}
