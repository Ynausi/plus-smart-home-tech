package ru.practicum.controller;

import org.springframework.web.bind.annotation.PostMapping;
import ru.practicum.dto.sensor.SensorEventDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.service.sensor.SensorService;

@RestController
@RequestMapping("/events/sensors")
@RequiredArgsConstructor
public class SensorController {
    private final SensorService sensorService;

    @PostMapping
    public ResponseEntity<Void> createSensor(@RequestBody @Valid SensorEventDto sensorEventDto) {
        sensorService.createSensor(sensorEventDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
