package ru.practicum.service.sensor;

import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

public interface SensorService {

    void createSensor(SensorEventProto proto);
}
