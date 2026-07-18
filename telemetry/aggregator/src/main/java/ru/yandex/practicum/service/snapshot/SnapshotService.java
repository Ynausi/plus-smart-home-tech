package ru.yandex.practicum.service.snapshot;

import ru.yandex.practicum.kafka.telemetry.sensor.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.sensor.SensorSnapshotAvro;

import java.util.Optional;

public interface SnapshotService {
    Optional<SensorSnapshotAvro> updateState(SensorEventAvro event);
}
