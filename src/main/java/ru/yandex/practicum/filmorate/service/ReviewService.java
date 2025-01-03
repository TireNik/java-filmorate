package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.List;

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
            log.info("Ошибка получения отзыва");
            throw new RuntimeException("Неизвестная ошибка при получении");
        }
    }

    public List<Review> getReviewsByFilm(Long id) {
        return List.of();
    }

    public void likeToReview(Long id) {

    }

    public void dislikeToReview(Long id) {

    }

    public void deleteLike(Long id) {

    }

    public void deleteDislike(Long id) {

    }
}
