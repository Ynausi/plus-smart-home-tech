package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "scenarios",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"hub_id", "name"})
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Scenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hub_id")
    private String hubId;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "scenario")
    private List<ScenarioCondition> conditions = new ArrayList<>();

    @OneToMany(mappedBy = "scenario")
    private List<ScenarioAction> actions = new ArrayList<>();


}
