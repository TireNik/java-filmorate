package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Validated
public class FilmController {

    private static final Logger logger = LoggerFactory.getLogger(FilmController.class);
    private final FilmStorage filmStorage;

    @Autowired
    public FilmController(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    @GetMapping
    public Collection<Film> getFilms() {
        logger.info("Запрос на получение всех фильмов.");
        return filmStorage.getFilm();
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        logger.info("Добавление фильма: {}", film.getName());
        Film saveFilm = filmStorage.addFilm(film);
        logger.info("Фильм успешно добавлен с ID: {}", film.getId());
        return saveFilm;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film newFilm) {
        logger.info("Обновление фильма с ID: {}", newFilm.getId());
        Film oldFilm = filmStorage.updateFilm(newFilm);
        logger.info("Фильм с ID: {} успешно обновлён.", newFilm.getId());
        return oldFilm;
    }

}
