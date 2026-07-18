package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.model.ScenarioCondition;
import ru.yandex.practicum.model.ScenarioConditionId;

public interface ScenarioConditionRepository extends JpaRepository<ScenarioCondition, ScenarioConditionId> {
}
