package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class Film {
    private Long id;
    private String name;
    @Size (max = 200)
    private String description;
    private LocalDate releaseDate;
    private int duration;
}
