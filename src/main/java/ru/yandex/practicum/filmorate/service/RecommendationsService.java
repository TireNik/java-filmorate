package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class RecommendationsService {
    private final FilmService filmService;

    @Autowired
    public RecommendationsService(FilmService filmService) {
        this.filmService = filmService;
    }

    public List<Film> getRecommendationsFilms(Long userId) {
        List<Long> friendsOfInterestIds = filmService.getFriendsOfInterest(userId);
        if (friendsOfInterestIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> recommendedFilms = filmService.getRecommendedFilms(userId, friendsOfInterestIds);
        List<Film> films = new ArrayList<>();
        for (Long id : recommendedFilms) {
            films.add(filmService.getFilm(id));
        }
        log.debug("Получаем список рекомендованных фильмов для пользавателя {}", userId);
        return films;
    }
}
