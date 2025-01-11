package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {
    Film getFilmById(Long id);

    Collection<Film> getFilms();

    Film addFilm(Film film);

    Film updateFilm(Film film);

    List<Film> getPopularFilms(int count,Integer genreId,Integer year);

    List<Film> getPopularCommonFilms(Long userId, Long friendId);
}
