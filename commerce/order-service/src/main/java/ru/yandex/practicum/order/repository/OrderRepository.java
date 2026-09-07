package ru.yandex.practicum.order.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.order.entity.OrderEntity;
import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity,Long> {
    List<OrderEntity> findAllByOrderByCreatedAtDesc();
    List<OrderEntity> findAllByCustomerEmailIgnoreCaseOrderByCreatedAtDesc(String email);
}
