package ru.yandex.practicum.inventory.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String m) {
        super(m);
    }
}
