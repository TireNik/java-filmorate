package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {
    Film getFilmById(Long id);

    Collection<Film> getFilms();

    List<Film> getFilmsByDirector(long directorId, String sortBy);

    Film addFilm(Film film);

    Film updateFilm(Film film);

    List<Film> getPopularFilms(int count, Integer genreId, Integer year);

    List<Film> getPopularCommonFilms(Long userId, Long friendId);

    void deleteFilm(Long id);

    List<Film> searchFilmsTitleAndDirector(String queryStr);

    List<Film> searchFilmsTitle(String queryStr);

    List<Film> searchFilmsDirector(String queryStr);

}
