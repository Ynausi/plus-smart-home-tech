package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "scenario_conditions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioCondition {

    @EmbeddedId
    private ScenarioConditionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("scenarioId")
    @JoinColumn(name = "scenario_id")
    private Scenario scenario;


    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("conditionId")
    @JoinColumn(name = "condition_id")
    private Condition condition;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("sensorId")
    @JoinColumn(name = "sensor_id")
    private Sensor sensor;
}
