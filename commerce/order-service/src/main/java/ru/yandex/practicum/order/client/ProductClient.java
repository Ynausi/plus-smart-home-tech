package ru.yandex.practicum.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.order.client.dto.ProductClientResponse;
import ru.yandex.practicum.order.client.fallback.ProductClientFallbackFactory;

@FeignClient(
        name = "product-service",
        path = "/api/products",
        fallbackFactory = ProductClientFallbackFactory.class
)
public interface ProductClient {

    @GetMapping("/{id}")
    ProductClientResponse findById(@PathVariable("id") Long id);
}
