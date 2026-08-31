package ru.yandex.practicum.product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.product.dto.*;
import ru.yandex.practicum.product.entity.*;
import ru.yandex.practicum.product.exception.NotFoundException;
import ru.yandex.practicum.product.repository.ProductRepository;
import java.util.List;

@Service @RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository repository;
    private final CategoryService categoryService;

    public List<ProductDto> findAll() { return repository.findAllByActiveTrue().stream().map(this::toDto).toList(); }
    public ProductDto findById(Long id) { return toDto(get(id)); }
    public List<ProductDto> findByCategory(Long categoryId) {
        categoryService.get(categoryId);
        return repository.findAllByCategoryIdAndActiveTrue(categoryId).stream().map(this::toDto).toList();
    }

    public List<ProductDto> search(String query) {
        return repository.findAllByNameContainingIgnoreCaseAndActiveTrue(query == null ? "" : query).stream().map(this::toDto).toList();
    }

    @Transactional
    public ProductDto create(CreateProductRequest r) {
        Product p = new Product();
        p.setName(r.name()); p.setDescription(r.description()); p.setPrice(r.price()); p.setImageUrl(r.imageUrl()); p.setActive(true);
        if (r.categoryId() != null) p.setCategory(categoryService.get(r.categoryId()));
        return toDto(repository.save(p));
    }

    @Transactional
    public ProductDto update(Long id, UpdateProductRequest r) {
        Product p = get(id);
        if (r.name() != null) p.setName(r.name());
        if (r.description() != null) p.setDescription(r.description());
        if (r.price() != null) p.setPrice(r.price());
        if (r.categoryId() != null) p.setCategory(categoryService.get(r.categoryId()));
        if (r.imageUrl() != null) p.setImageUrl(r.imageUrl());
        if (r.active() != null) p.setActive(r.active());
        return toDto(p);
    }

    private Product get(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Товар не найден: " + id));
    }

    private ProductDto toDto(Product p) {
        CategoryDto category = p.getCategory() == null ? null : categoryService.toDto(p.getCategory());
        return new ProductDto(p.getId(), p.getName(), p.getDescription(), p.getPrice(), category, p.getImageUrl(), p.getActive());
    }
}
