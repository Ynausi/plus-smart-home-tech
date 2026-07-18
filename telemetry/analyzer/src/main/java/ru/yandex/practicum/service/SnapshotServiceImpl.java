package ru.yandex.practicum.service;


import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.kafka.telemetry.sensor.*;
import ru.yandex.practicum.model.*;
import ru.yandex.practicum.repository.ScenarioRepository;

import java.time.Instant;
import java.util.List;


@Service
@RequiredArgsConstructor
public class SnapshotServiceImpl implements SnapshotService {

    private final ScenarioRepository scenarioRepository;

    @GrpcClient("hub-router")
    private HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterStub;


    @Transactional
    @Override
    public void handleSnapshot(SensorSnapshotAvro snapshotAvro) {
        List<Scenario> scenarios = scenarioRepository.findByHubId(snapshotAvro.getHubId());
        for (Scenario scenario:scenarios) {
            boolean ok = true;
            for (ScenarioCondition scenarioCondition: scenario.getConditions()) {
                if (!checkCondition(scenarioCondition,snapshotAvro)) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                System.out.println("SCENARIO FIRED: " + scenario.getName());
                for (ScenarioAction scenarioAction:scenario.getActions()) {
                    Action action = scenarioAction.getAction();
                    Sensor sensor = scenarioAction.getSensor();

                    DeviceActionProto.Builder builder = DeviceActionProto.newBuilder()
                            .setSensorId(sensor.getId())
                            .setType(ActionTypeProto.valueOf(action.getType()));

                    if (ActionTypeProto.valueOf(action.getType()) == ActionTypeProto.SET_VALUE) {
                        builder.setValue(action.getValue());
                    }

                    DeviceActionProto actionProto = builder.build();

                    Instant instant = snapshotAvro.getTimestamp();
                    Timestamp timestamp = Timestamp.newBuilder()
                            .setSeconds(instant.getEpochSecond())
                            .setNanos(instant.getNano())
                            .build();

                    DeviceActionRequest request = DeviceActionRequest.newBuilder()
                            .setHubId(scenario.getHubId())
                            .setScenarioName(scenario.getName())
                            .setAction(actionProto)
                            .setTimestamp(timestamp)
                            .build();
                    System.out.println(request);
                    try {
                        hubRouterStub.handleDeviceAction(request);
                        System.out.println("SEND OK");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private boolean checkCondition(ScenarioCondition scenarioCondition, SensorSnapshotAvro snapshotAvro) {
        Condition condition = scenarioCondition.getCondition();
        String sensorId = scenarioCondition.getSensor().getId();
        SensorStateAvro state = snapshotAvro.getSensorsState().get(sensorId);
        if (state == null) {
            return false;
        }
        switch (condition.getType()) {
            case "MOTION" -> {
                MotionSensorAvro data = (MotionSensorAvro) state.getData();

                return switch (condition.getOperation()) {
                    case "EQUALS" ->
                            data.getMotion() == (((Integer) condition.getValue())==1);
                    default -> false;
                };
            }
            case "TEMPERATURE" -> {
                ClimateSensorAvro data = (ClimateSensorAvro) state.getData();

                return switch (condition.getOperation()) {
                    case "LOWER_THAN" ->
                        data.getTemperatureC() < (Integer) condition.getValue();
                    default -> false;
                };
            }
            case "LUMINOSITY" -> {
                LightSensorAvro data = (LightSensorAvro) state.getData();

                return switch (condition.getOperation()) {
                    case "LOWER_THAN" ->
                        data.getLuminosity() < (Integer) condition.getValue();
                    default -> false;
                };
            }
            case "SWITCH" -> {
                SwitchSensorAvro data = (SwitchSensorAvro) state.getData();

                return switch ((condition.getOperation())) {
                    case "EQUALS" ->
                            data.getState() == (((Integer) condition.getValue()) == 1);
                    default -> false;
                };
            }
            default -> {
                return false;
            }
        }
    }
}
