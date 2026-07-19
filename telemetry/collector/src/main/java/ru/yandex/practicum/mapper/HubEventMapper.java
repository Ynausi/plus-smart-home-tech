package ru.yandex.practicum.mapper;

import org.mapstruct.*;
import ru.yandex.practicum.dto.hub.*;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.kafka.telemetry.hub.*;

@Mapper(componentModel = "spring",imports = HubEventType.class)
public interface HubEventMapper {

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

    default ScenarioConditionDto fromProto(ScenarioConditionProto proto) {
        ScenarioConditionDto dto = new ScenarioConditionDto();

        dto.setSensorId(proto.getSensorId());
        dto.setType(fromProto(proto.getType()));
        dto.setOperation(fromProto(proto.getOperation()));

        switch (proto.getValueCase()) {
            case BOOL_VALUE ->
                    dto.setValue(proto.getBoolValue() ? 1 : 0);
            case INT_VALUE ->
                    dto.setValue(proto.getIntValue());
            case VALUE_NOT_SET ->
                    dto.setValue(null);
        }

        return dto;
    }
}