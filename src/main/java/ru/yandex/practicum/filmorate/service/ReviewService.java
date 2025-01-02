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
        log.info("Добавление отзыва с id {}", reviews.getReviewId());
        try {
            return reviewStorage.addReviews(reviews);
        } catch (Exception e) {
            log.error("Неизвестная ошибка при добавлении пользователя: ", e);
            throw new RuntimeException("Неизвестная ошибка при добавлении пользователя", e);
        }
    }

    public Review updateReviews(Review reviews) {
        return null;
    }

    public void deleteReviews(Review reviews) {

    }

    public Review getReviewsById(Review reviews) {
        return null;
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
