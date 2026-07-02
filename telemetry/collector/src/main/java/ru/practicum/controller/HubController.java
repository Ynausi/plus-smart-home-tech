/*package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.hub.HubEventDto;
import ru.practicum.service.hub.HubService;

@RestController
@RequestMapping("/events/hubs")
@RequiredArgsConstructor
public class HubController {
    private final HubService hubService;

    @PostMapping
    public ResponseEntity<Void> createHub(@RequestBody @Valid HubEventDto dto) {
        hubService.createHub(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}*/
