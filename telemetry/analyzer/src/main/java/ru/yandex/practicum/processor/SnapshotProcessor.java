package ru.yandex.practicum.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.sensor.SensorSnapshotAvro;
import ru.yandex.practicum.service.SnapshotService;


@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotProcessor {
    private static final String SNAPSHOT_TOPIC = "telemetry.snapshots.v1";
    private static final String SNAPSHOT_CONTAINER_FACTORY = "snapshotKafkaListenerContainerFactory";
    private final SnapshotService snapshotService;

    @KafkaListener(topics = SNAPSHOT_TOPIC,
            containerFactory = SNAPSHOT_CONTAINER_FACTORY)
    public void handler(SensorSnapshotAvro snapshotAvro) {
        snapshotService.handleSnapshot(snapshotAvro);
    }
}
