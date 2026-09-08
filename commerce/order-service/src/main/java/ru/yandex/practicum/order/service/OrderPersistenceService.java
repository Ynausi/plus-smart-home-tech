package ru.yandex.practicum.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.order.entity.OrderEntity;
import ru.yandex.practicum.order.repository.OrderRepository;

@Service
@RequiredArgsConstructor
public class OrderPersistenceService {
    private final OrderRepository repository;

    @Transactional
    public OrderEntity save(OrderEntity order) {
        return repository.saveAndFlush(order);
    }
}
