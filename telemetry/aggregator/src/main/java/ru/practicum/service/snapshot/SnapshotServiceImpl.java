package ru.practicum.service.snapshot;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.sensor.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.sensor.SensorSnapshotAvro;
import ru.yandex.practicum.kafka.telemetry.sensor.SensorStateAvro;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SnapshotServiceImpl implements SnapshotService {
    private final Map<String,SensorSnapshotAvro> snapshots = new HashMap<>();

    @Override
    public Optional<SensorSnapshotAvro> updateState(SensorEventAvro event) {
        SensorSnapshotAvro snapshot = snapshots.computeIfAbsent(
                event.getHubId(),
                hubId-> {
                    SensorSnapshotAvro s = new SensorSnapshotAvro();
                    s.setHubId(hubId);
                    s.setTimestamp(event.getTimestamp());
                    s.setSensorsState(new HashMap<>());
                    return s;
                }
        );

        SensorStateAvro oldState = snapshot.getSensorsState().get(event.getId());

        if (oldState != null && (oldState.getTimestamp().isAfter(event.getTimestamp()) ||
                event.getPayload().equals(oldState.getData()))) return Optional.empty();

        SensorStateAvro state = new SensorStateAvro(event.getTimestamp(),event.getPayload());
        snapshot.getSensorsState().put(event.getId(), state);
        snapshot.setTimestamp(event.getTimestamp());
        return Optional.of(snapshot);
    }
}
