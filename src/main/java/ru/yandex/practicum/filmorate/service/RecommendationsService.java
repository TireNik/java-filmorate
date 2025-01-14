package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class RecommendationsService {
    private final FilmService filmService;
    private final UserService userService;

    @Autowired
    public RecommendationsService(FilmService filmService, UserService userService) {
        this.filmService = filmService;
        this.userService = userService;
    }

    public List<Film> getRecommendationsFilms(Long userId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new UserNotFoundException("Пользователь с ID " + userId + " не найден");
        }

        List<Long> friendsOfInterestIds = filmService.getFriendsOfInterest(userId);
        if (friendsOfInterestIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> recommendedFilms = filmService.getRecommendedFilms(userId, friendsOfInterestIds);

        List<Film> films = filmService.getFilmsByIds(recommendedFilms);

        log.debug("Получаем список рекомендованных фильмов для пользователя {}", userId);
        return films;
    }

}
