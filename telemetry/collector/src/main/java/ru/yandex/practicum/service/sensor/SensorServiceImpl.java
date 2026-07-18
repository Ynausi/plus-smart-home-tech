package ru.yandex.practicum.service.sensor;

import ru.yandex.practicum.dto.sensor.SensorEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.mapper.SensorEventMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.sensor.SensorEventAvro;

@Service
@RequiredArgsConstructor
@Slf4j
public class SensorServiceImpl implements SensorService{

    private final KafkaTemplate<String, SensorEventAvro> kafkaTemplate;
    private static final String SENSOR_TOPIC = "telemetry.sensors.v1";
    private final SensorEventMapper sensorMapper;

    @Override
    public void createSensor(SensorEventProto proto) {
        SensorEventDto dto = sensorMapper.fromProto(proto);
        SensorEventAvro avroEvent = sensorMapper.toAvro(dto);
        log.info("id = {},type = {}",dto.getId(),dto.getType());
        kafkaTemplate.send(SENSOR_TOPIC,dto.getHubId(),avroEvent);
    }
}
