package ru.practicum.dto.hub;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class ScenarioAddedEventDto extends HubEventDto {

    @NotBlank
    @Size(min = 3)
    private String name;

    @NotNull
    private List<ScenarioConditionDto> conditions;

    @NotNull
    private List<DeviceActionDto> actions;

    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_ADDED;
    }
}
