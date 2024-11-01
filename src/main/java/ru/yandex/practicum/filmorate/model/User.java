package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.WithoutSpace;

import java.time.LocalDate;

@Data
@Builder
public class User {

    private Long id;

    @Email
    @NotBlank
    @NotNull
    private String email;

    @NotBlank
    @NotNull
    @WithoutSpace
    private String login;

    private String name;

    @PastOrPresent
    @NotNull
    private LocalDate birthday;
}
