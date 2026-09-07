package ru.yandex.practicum.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.order.client.dto.InventoryOperationRequest;
import ru.yandex.practicum.order.client.dto.InventoryOperationResponse;

@FeignClient(name = "inventory-service", path = "/api/inventory")
public interface InventoryClient {

    @PostMapping("/reserve")
    InventoryOperationResponse reserve(@RequestBody InventoryOperationRequest request);

    @PostMapping("/release")
    InventoryOperationResponse release(@RequestBody InventoryOperationRequest request);
}
