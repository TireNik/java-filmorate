package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film addFilm(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.debug("Фильм сохранён в хранилище: ID = {}, Name = {}", film.getId(), film.getName());
        return film;
    }

    @Override
    public Collection<Film> getFilm() {
        return films.values();
    }

    @Override
    public Film updateFilm(Film newFilm) {

        if (newFilm.getId() == null) {
            log.error("Ошибка обновления: ID должен быть указан.");
            throw new ValidationException("Id должен быть указан");
        }
        if (!films.containsKey(newFilm.getId())) {
            log.error("Ошибка обновления: Фильм с указанным id не найден.");
            throw new ResourceNotFoundException("Фильм с указанным id не найден");
        }

        Film oldFilm = films.get(newFilm.getId());
        oldFilm.setName(newFilm.getName());
        oldFilm.setDescription(newFilm.getDescription());
        oldFilm.setDuration(newFilm.getDuration());
        oldFilm.setReleaseDate(newFilm.getReleaseDate());
        log.debug("Фильм обнавлен в хранилище: ID = {}, Name = {}", oldFilm.getId(), oldFilm.getName());

        return oldFilm;
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
