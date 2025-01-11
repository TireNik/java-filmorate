package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
@Qualifier("InMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage, LikeStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private final Map<Long, Set<Long>> likes = new HashMap<>();

    @Override
    public Collection<Film> getFilms() {
        return films.values();
    }

    @Override
    public void addLike(Film film, User user) {
        final Set<Long> idLikes = likes.computeIfAbsent(film.getId(), id -> new HashSet<>());
        idLikes.add(user.getId());
        log.info("Пользователь с ID {} поставил лайк фильму с ID {}", user.getId(), film.getId());
    }

    @Override
    public void deleteLike(Film film, User user) {
        final Set<Long> filmLikes = likes.computeIfAbsent(film.getId(), id -> new HashSet<>());
        filmLikes.remove(user.getId());
        log.info("Пользователь с ID {} удалил лайк у фильма с ID {}", user.getId(), user.getId());
    }

    @Override
    public List<Long> getFriendsOfInterestDB(Long userId) {
        return null;
    }

    ;

    @Override
    public List<Long> getRecommendedFilmsDB(Long userId, List<Long> friendsOfInterestIds) {
        return null;
    }

    @Override
    public List<Film> getPopularFilms(int count,Integer genreId,Integer year) {
        log.info("Получение {} популярных фильмов", count);

        return likes.entrySet().stream()
                .sorted((film1, film2) -> (film2.getValue().size()) - film1.getValue().size())
                .limit(count)
                .map(film1 -> getFilmById(film1.getKey()))
                .toList();
    }

    @Override
    public Film getFilmById(Long id) {
        if (!films.containsKey(id)) {
            throw new ResourceNotFoundException("Фильм с данным id не найден");
        }
        return films.get(id);
    }

    @Override
    public List<Film> getFilmsByDirector(long directorId, String sortBy) {
        return Collections.emptyList();
    }

    @Override
    public Film addFilm(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.debug("Фильм сохранён в хранилище: ID = {}, Name = {}", film.getId(), film.getName());
        return film;
    }

    @Override
    public Film updateFilm(Film newFilm) {

        if (newFilm.getId() == null) {
            log.error("Ошибка обновления: ID должен быть указан.");
            throw new ValidationException("Id должен быть указан");
        }
        if (!films.containsKey(newFilm.getId())) {
            log.error("Ошибка обновления: Фильм с указанным id {} не найден.", newFilm.getId());
            throw new ResourceNotFoundException(String.format("Фильм с указанным id %d не найден", newFilm.getId()));
        }

        films.put(newFilm.getId(), newFilm);
        log.debug("Фильм обнавлен в хранилище: ID = {}, Name = {}", newFilm.getId(), newFilm.getName());

        return newFilm;
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @Override
    public List<Film> getPopularCommonFilms(Long userId, Long friendId) {

        return null;
    }

    @Override
    public void deleteFilm(Long id) {

    }
}
