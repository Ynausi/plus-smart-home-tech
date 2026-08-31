package ru.yandex.practicum.product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.product.dto.*;
import ru.yandex.practicum.product.entity.Category;
import ru.yandex.practicum.product.exception.NotFoundException;
import ru.yandex.practicum.product.repository.CategoryRepository;
import java.util.List;

@Service @RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {
    private final CategoryRepository repository;
    public List<CategoryDto> findAll() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    public CategoryDto findById(Long id) {
        return toDto(get(id));
    }

    @Transactional
    public CategoryDto create(CreateCategoryRequest request) {
        Category c = new Category(); c.setName(request.name()); c.setDescription(request.description());
        return toDto(repository.save(c));
    }

    public Category get(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Категория не найдена: " + id));
    }

    public CategoryDto toDto(Category c) { return new CategoryDto(c.getId(), c.getName(), c.getDescription()); }
}
