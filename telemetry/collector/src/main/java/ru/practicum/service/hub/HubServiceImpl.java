package ru.practicum.service.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.dto.hub.HubEventDto;
import ru.practicum.mapper.HubEventMapper;
import ru.yandex.practicum.kafka.telemetry.hub.HubEventAvro;

@Service
@RequiredArgsConstructor
@Slf4j
public class HubServiceImpl implements HubService{
    private final HubEventMapper hubMapper;
    private final KafkaTemplate<String,HubEventAvro> kafkaTemplate;
    private static final String HUB_TOPIC = "telemetry.hubs.v1";

    @Override
    public void createHub(HubEventDto hubEventDto) {
        HubEventAvro avroHub = hubMapper.toAvro(hubEventDto);
        kafkaTemplate.send(HUB_TOPIC,hubEventDto.getHubId(),avroHub);
    }
}
