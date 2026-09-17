package ru.yandex.practicum.order.exception;

public class InventoryServiceUnavailableException extends RuntimeException {
    private final Long productId;
    private final String operation;

    public InventoryServiceUnavailableException(Long productId, String operation, Throwable cause) {
        super(
                "inventory-service технически недоступен при операции "
                        + operation + " для productId=" + productId,
                cause
        );
        this.productId = productId;
        this.operation = operation;
    }

    public Long getProductId() {
        return productId;
    }

    public String getOperation() {
        return operation;
    }
}
