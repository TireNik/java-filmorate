package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Data
public class User {
    private Long id;
    private String email;
    private String login;
    private String name;
    private Instant birthday;
}
