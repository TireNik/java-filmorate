package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final Map<Long, Set<Long>> likes = new HashMap<>();

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public void addLike(Long filmId, Long userId) {
        Film film = filmStorage.getFilmById(filmId);
        if (film == null) {
            throw new ResourceNotFoundException("Фильм с ID " + filmId + " не найден");
        }
        likes.computeIfAbsent(filmId, k -> new HashSet<>());

        if (!likes.get(filmId).add(userId)) {
            log.warn("Пользователь с ID {} уже поставил лайк фильму с ID {}", userId, filmId);
            throw new ValidationException("Пользователь уже поставил лайк этому фильму.");
        }
        log.info("Пользователь с ID {} поставил лайк фильму с ID {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        Film film = filmStorage.getFilmById(filmId);

        if (film == null) {
            throw new ResourceNotFoundException("Фильм с указанным id не найден");
        }
        if (!likes.containsKey(filmId) || !likes.get(filmId).remove(userId)) {
            log.warn("Лайк от пользователя с ID {} не найден у фильма с ID {}", userId, filmId);
            throw new ResourceNotFoundException("Лайк от пользователя не найден.");
        }

        log.info("Пользователь с ID {} удалил лайк у фильма с ID {}", userId, filmId);
    }

    public List<Film> getPopularFilms(int count) {
        return likes.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                .limit(count)
                .map(entry -> filmStorage.getFilmById(entry.getKey()))
                .collect(Collectors.toList());
    }
}
