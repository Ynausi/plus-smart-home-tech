package ru.yandex.practicum.order.controller;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.order.dto.*;
import ru.yandex.practicum.order.service.OrderService;
import java.util.List;


@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor public class OrderController {
    private final OrderService service;

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public OrderDto create(@Valid @RequestBody CreateOrderRequest r) {
        return service.create(r);
    }

    @GetMapping("/{id}") public OrderDto find (@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping public List<OrderDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/by-email") public List<OrderDto> byEmail(@RequestParam String email) {
        return service.findByEmail(email);
    }
}
