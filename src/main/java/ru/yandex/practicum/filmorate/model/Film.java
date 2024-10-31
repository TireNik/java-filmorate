package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;

@Data
public class Film {
    private Long id;
    private String name;
    private String description;
    private Instant releaseDate;
    private Duration duration;
}
