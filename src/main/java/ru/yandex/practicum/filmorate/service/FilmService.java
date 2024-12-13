package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage, MpaStorage mpaStorage, GenreStorage genreStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }

    public List<Genre> getAllGenres() {
        return genreStorage.getAllGenres();
    }

    public Optional<Genre> getGenreById(int id) {
        return genreStorage.getGenreById(id);
    }

    public List<Mpa> getAllMpa() {
        return mpaStorage.getAllMpa();
    }

    public Optional<Mpa> getMpaById(int id) {
        return mpaStorage.getMpaById(id);
    }

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