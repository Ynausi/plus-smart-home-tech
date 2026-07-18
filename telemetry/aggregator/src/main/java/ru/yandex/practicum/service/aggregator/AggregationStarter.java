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


    /*public void start() {
        try {
            consumer.subscribe(TOPICS);
            while (true) {
                ConsumerRecords<String,SensorEventAvro> records =
                        consumer.poll(Duration.ofSeconds(1));
                for(ConsumerRecord<String,SensorEventAvro> record:records) {
                    SensorEventAvro event = record.value();
                    Optional<SensorSnapshotAvro> snapshot = snapshotService.updateState(event);
                    snapshot.ifPresent(s->
                            producer.send(new ProducerRecord<>(
                                    SNAPSHOTS_TOPIC,
                                    s.getHubId(),
                                    s
                            ))
                    );
                }
            }
        } catch (WakeupException ignored) {
            // игнорируем - закрываем консьюмер и продюсер в блоке finally
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            try {
                producer.flush();
                consumer.commitSync();

            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                producer.close();
            }
        }
    }*/
}
