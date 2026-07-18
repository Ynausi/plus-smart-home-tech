package ru.yandex.practicum.service.aggregator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.kafka.telemetry.sensor.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.sensor.SensorSnapshotAvro;
import ru.yandex.practicum.service.snapshot.SnapshotService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {
    private static final String TOPICS = "telemetry.sensors.v1";
    private final KafkaTemplate<String, SensorSnapshotAvro> kafkaTemplate;
    private final SnapshotService snapshotService;
    private static final String SNAPSHOTS_TOPIC = "telemetry.snapshots.v1";

    @KafkaListener(topics = TOPICS)
    public void handle(SensorEventAvro event) {
        log.info("EVENT {} {} {}",
                event.getHubId(),
                event.getId(),
                event.getPayload());

        snapshotService.updateState(event)
                .ifPresent(s->
                        kafkaTemplate.send(
                                SNAPSHOTS_TOPIC,
                                s.getHubId(),
                                s
                        ));
    }
}
