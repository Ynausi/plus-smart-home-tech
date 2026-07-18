package ru.yandex.practicum.service.hub;

import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;

public interface HubService {

    void createHub(HubEventProto proto);
}
