package ru.yandex.practicum.service;

import ru.yandex.practicum.kafka.telemetry.hub.HubEventAvro;

public interface HubEventService {
    void handleEvent(HubEventAvro hubEvent);
}
