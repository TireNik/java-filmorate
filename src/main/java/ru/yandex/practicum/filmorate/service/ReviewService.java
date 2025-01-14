package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
@Slf4j
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public ReviewService(ReviewStorage reviewStorage, UserStorage userStorage,
                         @Qualifier("filmDbStorage") FilmStorage filmStorage) {
        this.reviewStorage = reviewStorage;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public Review addReviews(Review reviews) {
        Long userId = reviews.getUserId();
        if (userId == null || userId <= 0) {
            throw new ResourceNotFoundException("Некорректный ID пользователя: " + userId);
        }
        User user = userStorage.getUserById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Пользователь с ID " + userId + " не найден");
        }

        Long filmId = reviews.getFilmId();
        if (filmId == null || filmId <= 0) {
            throw new ResourceNotFoundException("Некорректный ID фильма: " + filmId);
        }
        Film film = filmStorage.getFilmById(filmId);
        if (film == null) {
            throw new ResourceNotFoundException("Фильм с ID " + filmId + " не найден");
        }

        log.info("Добавление отзыва");
        try {
            return reviewStorage.addReviews(reviews);
        } catch (Exception e) {
            log.error("Неизвестная ошибка при добавлении отзыва: ", e);
            throw new RuntimeException("Неизвестная ошибка при добавлении отзыва", e);
        }
    }


    public Review updateReviews(Review reviews) {
        try {
            log.info("Попытка обновить отзыв");
            return reviewStorage.updateReviews(reviews);
        } catch (Exception e) {
            log.info("Ошибка обновления");
            throw new RuntimeException("Неизвестная ошибка при обновлении");
        }
    }

    public void deleteReviews(Long id) {
        try {
            log.info("Попытка удалить отзыв");
            reviewStorage.deleteReviews(id);
            log.info("Отзыв удален");
        } catch (Exception e) {
            log.info("Ошибка удаления");
            throw new RuntimeException("Неизвестная ошибка при удалении");
        }
    }

    public Review getReviewsById(Long id) {
        try {
            log.info("Попытка получения отзыва");
            return reviewStorage.getReviewsById(id);
        } catch (Exception e) {
            log.info("Ошибка получения отзыва по Причине {}", e.getMessage());
            throw new ResourceNotFoundException("Неизвестная ошибка при получении");
        }
    }

    public List<Review> getReviewsByFilm(Long id, int count) {
        try {
            log.info("Пытаемся получить отзывы фильма");
            return reviewStorage.getReviewsByFilm(id, count);
        } catch (Exception e) {
            log.info("Ошибка получения отзывов фильма по Причине {}", e.getMessage());
            throw new RuntimeException("Ошибка получения отзывов фильма");
        }
    }

    public List<Review> getAllReviews(int count) {
        try {
            log.info("Пытаемся получить все отзывы");
            return reviewStorage.getAllReviews(count);
        } catch (Exception e) {
            log.info("Ошибка получения отзывов по Причине {}", e.getMessage());
            throw new RuntimeException("Ошибка получения всех Отзывов");
        }
    }

    public void likeToReview(Long reviewId, Long userId) {
        try {
            log.info("Попытка добавления лайка отзыву {} от пользователя {}", reviewId, userId);

            checkUserExists(userId);

            reviewStorage.likeToReview(reviewId, userId);
            log.info("Добавили лайк отзыву");
        } catch (Exception e) {
            log.info("Ошибка добавления лайка отзыву. Причина {}", e.getMessage());
            throw new RuntimeException("Неизвестная ошибка при добавление лайка отзыву");
        }
    }

    public void dislikeToReview(Long reviewId, Long userId) {
        try {
            log.info("Попытка добавления дислайка отзыву {} от пользователя {}", reviewId, userId);

            checkUserExists(userId);

            reviewStorage.dislikeToReview(reviewId, userId);
            log.info("Добавили дислайк отзыву");
        } catch (Exception e) {
            log.info("Ошибка добавления дислайка отзыву. Причина {}", e.getMessage());
            throw new RuntimeException("Неизвестная ошибка при добавление дислайка отзыву");
        }
    }

    public void deleteLike(Long reviewId, Long userId) {
        try {
            log.info("Попытка удаления лайка отзыву {} от пользователя {}", reviewId, userId);

            checkUserExists(userId);

            reviewStorage.deleteLike(reviewId, userId);
            log.info("Удаление лайка отзыву");
        } catch (Exception e) {
            log.info("Ошибка удаления лайка отзыву. Причина {}", e.getMessage());
            throw new RuntimeException("Неизвестная ошибка при удаление лайка отзыву");
        }
    }

    public void deleteDislike(Long reviewId, Long userId) {
        try {
            log.info("Попытка удаления дислайка отзыву {} от пользователя {}", reviewId, userId);

            checkUserExists(userId);

            reviewStorage.deleteDislike(reviewId, userId);
            log.info("Удаление дислайка отзыву");
        } catch (Exception e) {
            log.info("Ошибка удаления дислайка отзыву. Причина {}", e.getMessage());
            throw new RuntimeException("Неизвестная ошибка при удаление дислайка отзыву");
        }
    }

    public void checkUserExists(Long userId) {
        User user = userStorage.getUserById(userId);

        if (user == null) {
            log.info("Пользователь с ID {} не существует", userId);
            throw new UserNotFoundException("User not found");
        }
    }
}
