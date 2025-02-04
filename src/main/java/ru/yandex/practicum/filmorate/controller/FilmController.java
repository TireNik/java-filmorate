package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
@Validated
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @GetMapping
    public Collection<Film> getFilms() {
        log.info("Запрос на получение всех фильмов.");
        return filmService.getFilms();
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable long id) {
        log.info("получение фильма по id {}", id);
        return filmService.getFilm(id);
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        log.info("Добавляем фильм: {}", film);
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film newFilm) {
        log.info("Обновляем фильм с ID: {}", newFilm.getId());
        return filmService.updateFilm(newFilm);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Добавляем лайк: Film ID = {}, User ID = {}", id, userId);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Удаляем лайк: Film ID = {}, User ID = {}", id, userId);
        filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count, @RequestParam(required = false) Integer genreId, @RequestParam(required = false) Integer year) {
        log.info("Получение {} популярных фильмов", count);
        return filmService.getPopularFilms(count, genreId, year);
    }

    @GetMapping("/common")
    public List<Film> getPopularCommonFilms(@RequestParam Long userId, @RequestParam Long friendId) {
        log.info("Получение общих популярных фильмов друзей c id {} и {}", userId, friendId);
        return filmService.getPopularCommonFilms(userId, friendId);
    }

    @GetMapping("/director/{directorId}")
    public ResponseEntity<List<Film>> getFilmsByDirector(
            @PathVariable long directorId,
            @RequestParam(required = false, defaultValue = "year") String sortBy) {
        log.info("Получение всех фильмов режиссера, отсортированных по {}", sortBy);
        List<Film> films = filmService.getFilmsByDirector(directorId, sortBy);
        return ResponseEntity.ok(films);
    }

    @DeleteMapping("/{id}")
    public void deleteFilm(@PathVariable Long id) {
        log.info("Попытка удаления пользователя");
        filmService.deleteFilm(id);
    }


    @GetMapping(value = "search")
    public List<Film> searchFilmsTitleDirector(@RequestParam(required = false) String query,
                                                    @RequestParam(required = false) String by) {
        log.info("Получение фильмов по называнию {} и режисеру {} .", query, by);
        return filmService.searchFilms(query, by);
    }
}
