package ru.practicum.service.sensor;

import ru.practicum.dto.sensor.SensorEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.mapper.SensorEventMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.sensor.SensorEventAvro;

@Service
@RequiredArgsConstructor
@Slf4j
public class SensorServiceImpl implements SensorService{

    private final KafkaTemplate<String, SensorEventAvro> kafkaTemplate;
    private static final String SENSOR_TOPIC = "telemetry.sensors.v1";
    private final SensorEventMapper sensorMapper;

    @Override
    public void createSensor(SensorEventDto sensorEventDto) {
        SensorEventAvro avroEvent = sensorMapper.toAvro(sensorEventDto);
        log.info("id = {},type = {}",sensorEventDto.getId(),sensorEventDto.getType());
        kafkaTemplate.send(SENSOR_TOPIC,sensorEventDto.getHubId(),avroEvent);
    }
}
