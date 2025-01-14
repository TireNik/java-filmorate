package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;


public interface LikeStorage {
    void deleteLike(Film film, User user);

    void addLike(Film film, User user);

    List<Long> getFriendsOfInterestDB(Long userId);

    List<Long> getRecommendedFilmsDB(Long userId, List<Long> friendsOfInterestIds);
}
