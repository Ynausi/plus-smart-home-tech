package ru.yandex.practicum.mapper;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.sensor.*;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.kafka.telemetry.sensor.*;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class SensorEventConverter {

    private final SensorEventMapper mapper;

    public SensorEventDto fromProto(SensorEventProto proto) {

        return switch (proto.getPayloadCase()) {

            case LIGHT_SENSOR -> {
                LightSensorEventDto dto = mapper.fromProto(proto.getLightSensor());
                fill(dto, proto);
                yield dto;
            }

            case CLIMATE_SENSOR -> {
                ClimateSensorEventDto dto = mapper.fromProto(proto.getClimateSensor());
                fill(dto, proto);
                yield dto;
            }

            case MOTION_SENSOR -> {
                MotionSensorEventDto dto = mapper.fromProto(proto.getMotionSensor());
                fill(dto, proto);
                yield dto;
            }

            case SWITCH_SENSOR -> {
                SwitchSensorEventDto dto = mapper.fromProto(proto.getSwitchSensor());
                fill(dto, proto);
                yield dto;
            }

            case TEMPERATURE_SENSOR -> {
                TemperatureSensorEventDto dto = mapper.fromProto(proto.getTemperatureSensor());
                fill(dto, proto);
                yield dto;
            }

            case PAYLOAD_NOT_SET ->
                    throw new IllegalArgumentException("Payload not set");
        };
    }

    public SensorEventAvro toAvro(SensorEventDto dto) {

        Object payload = switch (dto.getType()) {

            case LIGHT_SENSOR_EVENT ->
                    mapper.toAvro((LightSensorEventDto) dto);

            case CLIMATE_SENSOR_EVENT ->
                    mapper.toAvro((ClimateSensorEventDto) dto);

            case MOTION_SENSOR_EVENT ->
                    mapper.toAvro((MotionSensorEventDto) dto);

            case SWITCH_SENSOR_EVENT ->
                    mapper.toAvro((SwitchSensorEventDto) dto);

            case TEMPERATURE_SENSOR_EVENT ->
                    mapper.toAvro((TemperatureSensorEventDto) dto);
        };

        return SensorEventAvro.newBuilder()
                .setId(dto.getId())
                .setHubId(dto.getHubId())
                .setTimestamp(dto.getTimestamp())
                .setPayload(payload)
                .build();
    }

    private <T extends SensorEventDto> T fill(T dto, SensorEventProto proto) {
        dto.setId(proto.getId());
        dto.setHubId(proto.getHubId());
        dto.setTimestamp(fromTimestamp(proto.getTimestamp()));
        return dto;
    }

    private Instant fromTimestamp(Timestamp timestamp) {
        return Instant.ofEpochSecond(
                timestamp.getSeconds(),
                timestamp.getNanos()
        );
    }
}
