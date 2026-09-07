package ru.yandex.practicum.order.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name="orders")
@Getter
@Setter
@NoArgsConstructor
public class OrderEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name="customer_name",nullable=false)
    private String customerName;

    @Column(name="customer_email",nullable=false)
    private String customerEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private OrderStatus status;

    @Column(name="total_price",nullable=false,precision=19,scale=2)
    private BigDecimal totalPrice;

    @Column(name="status_details",length=1000)
    private String statusDetails;

    @Column(name="created_at",nullable=false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy="order",cascade=CascadeType.ALL,orphanRemoval=true)
    @OrderBy("id ASC")
    private List<OrderItem> items=new ArrayList<>();

    public void addItem(OrderItem i) {
        items.add(i);i.setOrder(this);
    }
}
