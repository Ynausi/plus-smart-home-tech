package ru.practicum.dto.sensor;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LightSensorEventDto extends SensorEventDto{
    @NotNull
    private Integer linkQuality;

    @NotNull
    private Integer luminosity;
    @Override
    public SensorEventType getType() {
        return SensorEventType.LIGHT_SENSOR_EVENT;
    }
}
