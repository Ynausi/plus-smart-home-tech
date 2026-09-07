package ru.yandex.practicum.order.client.dto;

import java.math.BigDecimal;

public record ProductClientResponse(
        Long id,
        String name,
        BigDecimal price,
        Boolean active
) {
}
