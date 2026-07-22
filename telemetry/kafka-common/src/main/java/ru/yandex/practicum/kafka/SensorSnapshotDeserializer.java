package ru.yandex.practicum.kafka;

import ru.yandex.practicum.kafka.telemetry.sensor.SensorSnapshotAvro;

public class SensorSnapshotDeserializer extends BaseAvroDeserializer<SensorSnapshotAvro> {

    public SensorSnapshotDeserializer() {
        super(SensorSnapshotAvro.getClassSchema());
    }
}
