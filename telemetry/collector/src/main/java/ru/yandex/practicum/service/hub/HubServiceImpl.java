package ru.yandex.practicum.service.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.hub.HubEventDto;
import ru.yandex.practicum.mapper.HubEventMapper;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.hub.HubEventAvro;

@Service
@RequiredArgsConstructor
@Slf4j
public class HubServiceImpl implements HubService{
    private final HubEventMapper hubMapper;
    private final KafkaTemplate<String,HubEventAvro> kafkaTemplate;
    private static final String HUB_TOPIC = "telemetry.hubs.v1";

    @Override
    public void createHub(HubEventProto proto) {
        log.info("PROTO = {}", proto);
        HubEventDto dto = hubMapper.fromProto(proto);
        log.info("DTO = {}", dto);
        HubEventAvro avroHub = hubMapper.toAvro(dto);
        log.info("AVRO = {}", avroHub);
        kafkaTemplate.send(HUB_TOPIC,dto.getHubId(),avroHub);
    }
}
