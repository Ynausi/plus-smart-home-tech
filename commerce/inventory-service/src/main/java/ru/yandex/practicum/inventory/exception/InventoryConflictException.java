package ru.yandex.practicum.inventory.exception;

public class InventoryConflictException extends RuntimeException {
    public InventoryConflictException(String m) {
        super(m);
    }
}
