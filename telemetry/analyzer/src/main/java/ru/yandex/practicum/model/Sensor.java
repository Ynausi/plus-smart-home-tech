package ru.yandex.practicum.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sensors",schema = "public")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Sensor {

    @Id
    private String id;

    @Column(name = "hub_id")
    private String hubId;
}
