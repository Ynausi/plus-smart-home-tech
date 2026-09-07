package ru.yandex.practicum.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(
        @NotBlank(message = "Название категории обязательно")
        @Size(max = 255, message = "Название не может быть длиннее 255 символов")
        String name,
        @Size(max = 2000, message = "Описание не может быть длиннее 2000 символов")
        String description
) {}
