package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.dto.hub.*;
import ru.yandex.practicum.kafka.telemetry.hub.*;

import java.time.Instant;

@Mapper(componentModel = "spring",imports = HubEventType.class)
public interface HubEventMapper {

    default HubEventAvro toAvro(HubEventDto dto) {
        Object payload = switch (dto.getType()) {
            case DEVICE_ADDED -> toAvro((DeviceAddedEventDto) dto);
            case DEVICE_REMOVED -> toAvro((DeviceRemovedEventDto) dto);
            case SCENARIO_ADDED -> toAvro((ScenarioAddedEventDto) dto);
            case SCENARIO_REMOVED -> toAvro((ScenarioRemovedEventDto) dto);
        };
        return HubEventAvro.newBuilder()
                .setHubId(dto.getHubId())
                .setTimestamp(dto.getTimestamp())
                .setPayload(payload)
                .build();
    }

    @Mapping(target = "type",source = "deviceType")
    DeviceAddedEventAvro toAvro(DeviceAddedEventDto dto);

    DeviceRemovedEventAvro toAvro(DeviceRemovedEventDto dto);

    @Mapping(target = "conditions", source = "conditions")
    @Mapping(target = "actions", source = "actions")
    ScenarioAddedEventAvro toAvro(ScenarioAddedEventDto dto);

    ScenarioRemovedEventAvro toAvro(ScenarioRemovedEventDto dto);

    @Mapping(target = "sensorId", source = "sensorId")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "operation", source = "operation")
    @Mapping(target = "value", source = "value")
    ScenarioConditionAvro toAvro(ScenarioConditionDto dto);

    @Mapping(target = "sensorId", source = "sensorId")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "value", source = "value")
    DeviceActionAvro toAvro(DeviceActionDto dto);


    @Named("instantToMillis")
    default long instantToMillis(Instant instant) {
        return instant == null
                ? Instant.now().toEpochMilli()
                : instant.toEpochMilli();
    }
}
