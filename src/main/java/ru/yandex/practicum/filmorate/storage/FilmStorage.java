package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {
    Film getFilmById(Long id);

    Collection<Film> getFilms();

    Film addFilm(Film film);

    Film updateFilm(Film film);

    void addLike(Film film, User user);

    void deleteLike(Film film, User user);

    List<Film> getPopularFilms(int count);
}
