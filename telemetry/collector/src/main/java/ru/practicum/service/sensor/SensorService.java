package ru.practicum.service.sensor;

import ru.practicum.dto.sensor.SensorEventDto;

public interface SensorService {

    void createSensor(SensorEventDto sensorEventDto);
}
