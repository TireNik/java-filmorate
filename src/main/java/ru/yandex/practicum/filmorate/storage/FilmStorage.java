package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Film addFilm (Film film);
    Collection<Film> getFilm();
    Film updateFilm(Film newFilm);
}
