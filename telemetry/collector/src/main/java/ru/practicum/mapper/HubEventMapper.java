package ru.practicum.mapper;

import com.google.protobuf.Timestamp;
import org.mapstruct.*;
import ru.practicum.dto.hub.*;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.kafka.telemetry.hub.*;

import java.time.Instant;

@Mapper(componentModel = "spring",imports = HubEventType.class)
public interface HubEventMapper {

    default HubEventDto fromProto(HubEventProto proto) {
        return switch (proto.getPayloadCase()) {
            case DEVICE_ADDED -> {
                DeviceAddedEventDto dto = fromProto(proto.getDeviceAdded());
                dto.setHubId(proto.getHubId());
                dto.setTimestamp(fromTimestamp(proto.getTimestamp()));
                yield dto;
            }
            case DEVICE_REMOVED -> {
                DeviceRemovedEventDto dto = fromProto(proto.getDeviceRemoved());
                dto.setHubId(proto.getHubId());
                dto.setTimestamp(fromTimestamp(proto.getTimestamp()));
                yield dto;
            }
            case SCENARIO_ADDED -> {
                ScenarioAddedEventDto dto = fromProto(proto.getScenarioAdded());
                dto.setHubId(proto.getHubId());
                dto.setTimestamp(fromTimestamp(proto.getTimestamp()));
                yield dto;
            }
            case SCENARIO_REMOVED -> {
                ScenarioRemovedEventDto dto = fromProto(proto.getScenarioRemoved());
                dto.setHubId(proto.getHubId());
                dto.setTimestamp(fromTimestamp(proto.getTimestamp()));
                yield dto;
            }
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("Payload not set");
        };
    }

    @Mapping(target = "hubId", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    @Mapping(target = "deviceType",source = "type")
    DeviceAddedEventDto fromProto(DeviceAddedEventProto proto);

    @Mapping(target = "hubId", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    DeviceRemovedEventDto fromProto(DeviceRemovedEventProto proto);

    @Mapping(target = "hubId", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    @Mapping(target = "conditions", source = "conditionsList")
    @Mapping(target = "actions", source = "actionsList")
    ScenarioAddedEventDto fromProto(ScenarioAddedEventProto proto);

    @Mapping(target = "hubId", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    ScenarioRemovedEventDto fromProto(ScenarioRemovedEventProto proto);

    default ScenarioConditionDto fromProto(ScenarioConditionProto proto) {
        ScenarioConditionDto dto = new ScenarioConditionDto();

        dto.setSensorId(proto.getSensorId());
        dto.setType(fromProto(proto.getType()));
        dto.setOperation(fromProto(proto.getOperation()));

        switch (proto.getValueCase()) {
            case BOOL_VALUE -> dto.setValue(proto.getBoolValue() ? 1 : 0);
            case INT_VALUE -> dto.setValue(proto.getIntValue());
            case VALUE_NOT_SET -> dto.setValue(null);
        }

        return dto;
    }

    @Mapping(target = "sensorId", source = "sensorId")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "value", source = "value")
    DeviceActionDto fromProto(DeviceActionProto proto);

    @ValueMapping(source = "UNRECOGNIZED",
            target = MappingConstants.THROW_EXCEPTION)
    DeviceTypeDto fromProto(DeviceTypeProto proto);

    @ValueMapping(source = "UNRECOGNIZED",
            target = MappingConstants.THROW_EXCEPTION)
    DeviceActionType fromProto(ActionTypeProto proto);

    @ValueMapping(source = "UNRECOGNIZED",
            target = MappingConstants.THROW_EXCEPTION)
    ScenarioConditionType fromProto(ConditionTypeProto proto);

    @ValueMapping(source = "UNRECOGNIZED",
            target = MappingConstants.THROW_EXCEPTION)


    ScenarioConditionOperation fromProto(ConditionOperationProto proto);
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

    default Instant fromTimestamp(Timestamp timestamp) {
        return Instant.ofEpochSecond(
                timestamp.getSeconds(),
                timestamp.getNanos()
        );
    }
}