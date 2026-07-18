package ru.yandex.practicum.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.hub.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.sensor.SensorSnapshotAvro;
import ru.yandex.practicum.service.HubEventService;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventProcessor {
    private static final String HUB_TOPIC = "telemetry.hubs.v1";
    private final HubEventService hubEventService;
    private static final String HUB_CONTAINER_FACTORY = "hubKafkaListenerContainerFactory";

    @KafkaListener(
            topics = HUB_TOPIC,
            containerFactory = HUB_CONTAINER_FACTORY)
    public void handle(HubEventAvro hubEvent) {
        hubEventService.handleEvent(hubEvent);
    }
}
