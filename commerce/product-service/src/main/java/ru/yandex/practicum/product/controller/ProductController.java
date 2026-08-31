package ru.yandex.practicum.product.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.product.dto.*;
import ru.yandex.practicum.product.service.ProductService;
import java.util.List;

@RestController @RequestMapping("/api/products") @RequiredArgsConstructor
public class ProductController {
    private final ProductService service;

    @GetMapping public List<ProductDto> all() {
        return service.findAll();
    }

    @GetMapping("/{id}") public ProductDto byId(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/category/{categoryId}") public List<ProductDto> byCategory(@PathVariable Long categoryId) {
        return service.findByCategory(categoryId);
    }

    @GetMapping("/search") public List<ProductDto> search(@RequestParam String query) {
        return service.search(query);
    }

    @PostMapping public ResponseEntity<ProductDto> create(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PatchMapping("/{id}") public ProductDto update(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest request) {
        return service.update(id, request);
    }
}
