package ru.yandex.practicum.inventory.exception;

public class InvalidReservationReleaseException extends RuntimeException {

    public InvalidReservationReleaseException(String message) {
        super(message);
    }
}
