package ru.yandex.practicum.order.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String m) {
        super(m);
    }
}
