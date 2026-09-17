package ru.yandex.practicum.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.order.client.dto.InventoryOperationRequest;
import ru.yandex.practicum.order.client.dto.InventoryOperationResponse;
import ru.yandex.practicum.order.client.fallback.InventoryClientFallbackFactory;

@FeignClient(
        name = "inventory-service",
        path = "/api/inventory",
        fallbackFactory = InventoryClientFallbackFactory.class
)
public interface InventoryClient {

    @PostMapping("/reserve")
    InventoryOperationResponse reserve(@RequestBody InventoryOperationRequest request);

    @PostMapping("/release")
    InventoryOperationResponse release(@RequestBody InventoryOperationRequest request);
}
