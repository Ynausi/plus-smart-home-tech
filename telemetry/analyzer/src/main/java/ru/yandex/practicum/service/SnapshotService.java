package ru.yandex.practicum.service;

import ru.yandex.practicum.kafka.telemetry.sensor.SensorSnapshotAvro;

public interface SnapshotService {
    void handleSnapshot(SensorSnapshotAvro snapshotAvro);
}
