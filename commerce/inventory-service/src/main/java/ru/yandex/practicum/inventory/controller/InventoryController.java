package ru.yandex.practicum.inventory.controller;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.inventory.dto.*;
import ru.yandex.practicum.inventory.service.InventoryService;
import java.util.List;

@RestController @RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService service;
    @GetMapping public List<InventoryDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{productId}")
    public InventoryDto find(@PathVariable Long productId) {
        return service.findByProductId(productId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryDto create (@Valid @RequestBody UpdateInventoryRequest r) {
        return service.create(r);
    }

    @PutMapping public InventoryDto update (@Valid @RequestBody UpdateInventoryRequest r) {
        return service.update(r);
    }
    @PostMapping("/reserve") public ReserveResponse reserve (@Valid @RequestBody ReserveRequest r) {
        return service.reserve(r);
    }
}
