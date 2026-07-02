package ru.practicum.service.hub;

import ru.practicum.dto.hub.HubEventDto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;

public interface HubService {

    void createHub(HubEventProto proto);
}
