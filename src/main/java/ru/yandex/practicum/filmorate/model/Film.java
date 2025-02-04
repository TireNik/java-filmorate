package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.filmorate.validation.ReleaseDate;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Film {

    Long id;

    @NotBlank(message = "Название не может быть пустым")
    String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    @NotNull
    String description;

    @NotNull
    @ReleaseDate
    LocalDate releaseDate;

    @Min(value = 1, message = "Продолжительность фильма должна быть положительным числом")
    int duration;

    Mpa mpa;
    final LinkedHashSet<Genre> genres = new LinkedHashSet<>();

    public void setGenres(Collection<Genre> genres) {
        this.genres.clear();
        this.genres.addAll(genres);
    }

    final LinkedHashSet<Director> directors = new LinkedHashSet<>();

    public void setDirectors(Collection<Director> directors) {
        this.directors.clear();
        this.directors.addAll(directors);
    }
}
