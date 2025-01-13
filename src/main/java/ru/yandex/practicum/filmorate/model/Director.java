package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Director {
    Long id;
    @NotBlank(message = "Имя режиссера не может быть пустым")
    String name;
}