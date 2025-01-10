package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.Collection;

@Service
@Slf4j
public class ReviewService {
    private final ReviewStorage reviewStorage;

    public ReviewService(ReviewStorage reviewStorage) {
        this.reviewStorage = reviewStorage;
    }


    public Review addReviews(Review reviews) {
        log.info("Добавление отзыва");
        try {
            return reviewStorage.addReviews(reviews);
        } catch (Exception e) {
            log.error("Неизвестная ошибка при добавлении пользователя: ", e);
            throw new RuntimeException("Неизвестная ошибка при добавлении пользователя", e);
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
            throw new RuntimeException("Неизвестная ошибка при получении");
        }
    }

    public Collection<Review> getReviewsByFilm(Long id, int count) {
        try {
            log.info("Пытаемся получить отзывы фильма");
            return reviewStorage.getReviewsByFilm(id, count);
        } catch (Exception e) {
            log.info("Ошибка получения отзывов фильма по Причине {}", e.getMessage());
            throw new RuntimeException("Ошибка получения отзывов фильма");
        }
    }

    public Collection<Review> getAllReviews(int count) {
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
            reviewStorage.deleteDislike(reviewId, userId);
            log.info("Удаление дислайка отзыву");
        } catch (Exception e) {
            log.info("Ошибка удаления дислайка отзыву. Причина {}", e.getMessage());
            throw new RuntimeException("Неизвестная ошибка при удаление дислайка отзыву");
        }
    }
}
