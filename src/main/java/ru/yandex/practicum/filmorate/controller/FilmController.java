package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {

    private static final Logger logger = LoggerFactory.getLogger(FilmController.class);
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> getFilms() {
        logger.info("Запрос на получение всех фильмов.");
        return films.values();
    }

    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        logger.info("Добавление фильма: {}", film.getName());
        validation(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        logger.info("Фильм успешно добавлен с ID: {}", film.getId());
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film newFilm) {
        logger.info("Обновление фильма с ID: {}", newFilm.getId());

        if (newFilm.getId() == null) {
            logger.error("Ошибка обновления: ID должен быть указан.");
            throw new ValidationException("Id должен быть указан");
        }
        if (!films.containsKey(newFilm.getId())) {
            logger.error("Ошибка обновления: Фильм с указанным id не найден.");
            throw new ValidationException("Фильм с указанным id не найден");
        }

        validation(newFilm);

        Film oldFilm = films.get(newFilm.getId());
        oldFilm.setName(newFilm.getName());
        oldFilm.setDescription(newFilm.getDescription());
        oldFilm.setDuration(newFilm.getDuration());
        oldFilm.setReleaseDate(newFilm.getReleaseDate());

        logger.info("Фильм с ID: {} успешно обновлён.", newFilm.getId());
        return oldFilm;
    }

    private void validation(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            logger.error("Ошибка валидации: Название не может быть пустым.");
            throw new ValidationException("Название не может быть пустым");
        }

        if (film.getDescription().length() > 200) {
            logger.error("Ошибка валидации: Максимальная длина описания — 200 символов.");
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }

        if (releaseLimit(film.getReleaseDate())) {
            logger.error("Ошибка валидации: Дата релиза — не раньше 28 декабря 1895 года.");
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }

        if (film.getDuration() <= 0) {
            logger.error("Ошибка валидации: Продолжительность фильма должна быть положительным числом.");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }

        logger.info("Фильм {} успешно прошёл валидацию.", film.getName());
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private boolean releaseLimit(LocalDate date) {
        LocalDate limit = LocalDate.of(1895, 12, 28);
        return date.isBefore(limit);
    }

}
