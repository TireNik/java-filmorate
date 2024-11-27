package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Collection<Film> getFilms() {
        log.info("Получение всех фильмов.");
        return filmStorage.getFilms();
    }

    public Film getFilm(long filmId) {
        log.info("Получение фильма с ID {}", filmId);
        return filmStorage.getFilmById(filmId);
    }

    public Film addFilm(Film film) {
        log.info("Добавление фильма: {}", film.getName());
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        filmStorage.getFilmById(film.getId());
        log.info("Обновление фильма: {}", film.getName());
        return filmStorage.updateFilm(film);
    }

    public void addLike(long filmId, long userId) {
        log.info("Добавление лайка от пользователя с ID {} для фильма с ID {}", userId, filmId);
        filmStorage.addLike(filmStorage.getFilmById(filmId), userStorage.getUserById(userId));
    }

    public void deleteLike(long filmId, long userId) {
        log.info("Удаление лайка от пользователя с ID {} для фильма с ID {}", userId, filmId);
        filmStorage.deleteLike(filmStorage.getFilmById(filmId), userStorage.getUserById(userId));
    }

    public List<Film> getPopularFilms(int count) {
        log.info("Возврат топ-{} популярных фильмов", count);
        return filmStorage.getPopularFilms(count);
    }
}