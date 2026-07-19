package ru.yandex.practicum.mapper;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.hub.*;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.kafka.telemetry.hub.*;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class HubEventConverter {


    private final HubEventMapper mapper;

    public HubEventDto fromProto(HubEventProto proto) {
        return switch (proto.getPayloadCase()) {
            case DEVICE_ADDED -> {
                DeviceAddedEventDto dto = mapper.fromProto(proto.getDeviceAdded());
                fillCommonFields(dto, proto);
                yield dto;
            }
            case DEVICE_REMOVED -> {
                DeviceRemovedEventDto dto = mapper.fromProto(proto.getDeviceRemoved());
                fillCommonFields(dto, proto);
                yield dto;
            }
            case SCENARIO_ADDED -> {
                ScenarioAddedEventDto dto = mapper.fromProto(proto.getScenarioAdded());
                fillCommonFields(dto, proto);
                yield dto;
            }
            case SCENARIO_REMOVED -> {
                ScenarioRemovedEventDto dto = mapper.fromProto(proto.getScenarioRemoved());
                fillCommonFields(dto, proto);
                yield dto;
            }
            case PAYLOAD_NOT_SET ->
                    throw new IllegalArgumentException("Payload not set");
        };
    }


    public HubEventAvro toAvro(HubEventDto dto) {
        Object payload = switch (dto.getType()) {
            case DEVICE_ADDED ->
                    mapper.toAvro((DeviceAddedEventDto) dto);
            case DEVICE_REMOVED ->
                    mapper.toAvro((DeviceRemovedEventDto) dto);
            case SCENARIO_ADDED ->
                    mapper.toAvro((ScenarioAddedEventDto) dto);
            case SCENARIO_REMOVED ->
                    mapper.toAvro((ScenarioRemovedEventDto) dto);
        };

        return HubEventAvro.newBuilder()
                .setHubId(dto.getHubId())
                .setTimestamp(dto.getTimestamp())
                .setPayload(payload)
                .build();
    }

    private void fillCommonFields(HubEventDto dto, HubEventProto proto) {
        dto.setHubId(proto.getHubId());
        dto.setTimestamp(fromTimestamp(proto.getTimestamp()));
    }

    private Instant fromTimestamp(Timestamp timestamp) {
        return Instant.ofEpochSecond(
                timestamp.getSeconds(),
                timestamp.getNanos()
        );
    }
}
