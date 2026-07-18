package ru.yandex.practicum.kafka;

import ru.yandex.practicum.kafka.telemetry.hub.HubEventAvro;

public class HubEventDeserializer
        extends BaseAvroDeserializer<HubEventAvro> {

    public HubEventDeserializer() {
        super(HubEventAvro.getClassSchema());
    }
}