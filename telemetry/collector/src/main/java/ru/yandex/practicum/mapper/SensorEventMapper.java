package ru.yandex.practicum.mapper;

import com.google.protobuf.Timestamp;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.yandex.practicum.dto.sensor.*;
import ru.yandex.practicum.dto.sensor.*;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.kafka.telemetry.sensor.*;

import java.time.Instant;

@Mapper(componentModel = "spring",imports = SensorEventType.class)
public interface SensorEventMapper {

    @Mapping(target = "hubId", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    @Mapping(target = "id",ignore = true)
    LightSensorEventDto fromProto(LightSensorProto proto);

    @Mapping(target = "hubId", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    @Mapping(target = "id",ignore = true)
    MotionSensorEventDto fromProto(MotionSensorProto proto);

    @Mapping(target = "hubId", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    @Mapping(target = "id",ignore = true)
    SwitchSensorEventDto fromProto(SwitchSensorProto proto);

    @Mapping(target = "hubId", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    @Mapping(target = "id",ignore = true)
    TemperatureSensorEventDto fromProto(TemperatureSensorProto proto);

    @Mapping(target = "hubId", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    @Mapping(target = "id",ignore = true)
    ClimateSensorEventDto fromProto(ClimateSensorProto proto);

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

}
