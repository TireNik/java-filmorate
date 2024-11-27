package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {

    Long id;

    @Email(message = "Электронная почта должна содержать символ @")
    @NotBlank(message = "Электронная почта не может быть пустой")
    String email;

    @NotBlank(message = "Логин не может быть пустым")
    String login;

    String name;

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    @NotNull
    LocalDate birthday;

    Set<Long> friends = new HashSet<>();
}
