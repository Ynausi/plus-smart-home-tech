package ru.yandex.practicum.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;


import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.hub.*;
import ru.yandex.practicum.model.*;
import ru.yandex.practicum.repository.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@Service
@RequiredArgsConstructor
public class HubEventServiceImpl implements HubEventService{
    private final ActionRepository actionRepository;
    private final ConditionRepository conditionRepository;
    private final ScenarioRepository scenarioRepository;
    private final SensorRepository sensorRepository;
    private final ScenarioConditionRepository scenarioConditionRepository;
    private final ScenarioActionRepository scenarioActionRepository;
    private final Map<Class<?>, BiConsumer<HubEventAvro,Object>> handlers = new HashMap<>();


    private void handleDeviceAdded(HubEventAvro hubEvent,DeviceAddedEventAvro event) {
        Sensor sensor = new Sensor();
        sensor.setId(event.getId());
        sensor.setHubId(hubEvent.getHubId());
        sensorRepository.save(sensor);
    }

    private void handleDeviceRemoved(HubEventAvro hubEvent,DeviceRemovedEventAvro event) {
        sensorRepository.deleteById(event.getId());
    }

    private void handleScenarioAdded(HubEventAvro hubEvent,ScenarioAddedEventAvro event) {
        Scenario scenario = new Scenario();
        scenario.setHubId(hubEvent.getHubId());
        scenario.setName(event.getName());
        scenarioRepository.save(scenario);
        for (ScenarioConditionAvro conditionAvro:event.getConditions()) {
            Sensor sensor = sensorRepository.findById(conditionAvro.getSensorId())
                    .orElseThrow();

            Condition condition = new Condition();
            condition.setType(conditionAvro.getType().name());
            condition.setOperation(conditionAvro.getOperation().name());
            condition.setValue((Integer) conditionAvro.getValue());

            condition = conditionRepository.save(condition);

            ScenarioCondition scenarioCondition = new ScenarioCondition();
            scenarioCondition.setId(
                    new ScenarioConditionId(
                            scenario.getId(),
                            sensor.getId(),
                            condition.getId()
                    )
            );

            scenarioCondition.setScenario(scenario);
            scenarioCondition.setCondition(condition);
            scenarioCondition.setSensor(sensor);

            scenarioConditionRepository.save(scenarioCondition);
        }
        for (DeviceActionAvro actionAvro : event.getActions()) {
            Sensor sensor = sensorRepository.findById(actionAvro.getSensorId())
                    .orElseThrow();

            Action action = new Action();
            action.setType(actionAvro.getType().name());
            action.setValue(actionAvro.getValue());

            action = actionRepository.save(action);

            ScenarioAction scenarioAction = new ScenarioAction();

            scenarioAction.setId(
                    new ScenarioActionId(
                            scenario.getId(),
                            sensor.getId(),
                            action.getId()
                    )
            );

            scenarioAction.setScenario(scenario);
            scenarioAction.setAction(action);
            scenarioAction.setSensor(sensor);

            scenarioActionRepository.save(scenarioAction);
        }
    }

    private void handleScenarioRemoved(HubEventAvro hubEvent,ScenarioRemovedEventAvro event) {
        scenarioRepository.findByHubIdAndName(
                hubEvent.getHubId(),
                event.getName()
        ).ifPresent(scenarioRepository::delete);
    }

    private <T> void register(Class<T> type,
                              BiConsumer<HubEventAvro, T> handler) {
        handlers.put(type,
                (hubEvent,obj) -> handler.accept(hubEvent, type.cast(obj)));
    }

    @PostConstruct
    public void init() {
        register(DeviceAddedEventAvro.class, this::handleDeviceAdded);
        register(DeviceRemovedEventAvro.class, this::handleDeviceRemoved);
        register(ScenarioAddedEventAvro.class, this::handleScenarioAdded);
        register(ScenarioRemovedEventAvro.class, this::handleScenarioRemoved);
    }

    @Override
    public void handleEvent(HubEventAvro hubEvent) {
        Object payload = hubEvent.getPayload();

        BiConsumer<HubEventAvro,Object> handler = handlers.get(payload.getClass());

        if (handler == null) {
            throw new IllegalArgumentException(
                    "Unknown payload type: " + payload.getClass());
        }

        handler.accept(hubEvent,payload);
    }
}
