package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.dto.sensor.*;
import ru.yandex.practicum.kafka.telemetry.sensor.*;

import java.time.Instant;

@Mapper(componentModel = "spring",imports = SensorEventType.class)
public interface SensorEventMapper {

    default SensorEventAvro toAvro(SensorEventDto dto) {

        Object payload = switch (dto.getType()) {
            case LIGHT_SENSOR_EVENT -> toAvro((LightSensorEventDto) dto);
            case CLIMATE_SENSOR_EVENT -> toAvro((ClimateSensorEventDto) dto);
            case MOTION_SENSOR_EVENT -> toAvro((MotionSensorEventDto) dto);
            case SWITCH_SENSOR_EVENT -> toAvro((SwitchSensorEventDto) dto);
            case TEMPERATURE_SENSOR_EVENT -> toAvro((TemperatureSensorEventDto) dto);
        };
        return SensorEventAvro.newBuilder()
                .setId(dto.getId())
                .setHubId(dto.getHubId())
                .setTimestamp(dto.getTimestamp())
                .setPayload(payload)
                .build();
    }

    @Mapping(target = "temperatureC", source = "temperatureC")
    @Mapping(target = "humidity", source = "humidity")
    @Mapping(target = "co2Level", source = "co2Level")
    ClimateSensorAvro toAvro(ClimateSensorEventDto dto);

    @Mapping(target = "linkQuality", source = "linkQuality")
    @Mapping(target = "luminosity", source = "luminosity")
    LightSensorAvro toAvro(LightSensorEventDto dto);

    @Mapping(target = "linkQuality", source = "linkQuality")
    @Mapping(target = "motion", source = "motion")
    @Mapping(target = "voltage", source = "voltage")
    MotionSensorAvro toAvro(MotionSensorEventDto dto);

    @Mapping(target = "state", source = "state")
    SwitchSensorAvro toAvro(SwitchSensorEventDto dto);

    @Mapping(target = "temperatureC", source = "temperatureC")
    @Mapping(target = "temperatureF", source = "temperatureF")
    TemperatureSensorAvro toAvro(TemperatureSensorEventDto dto);

    @Named("instantToMillis")
    default long instantToMillis(Instant instant) {
        return instant == null
                ? Instant.now().toEpochMilli()
                : instant.toEpochMilli();
    }
}
