package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

public interface ReviewStorage {
    Review addReviews(Review reviews);

    Review updateReviews(Review reviews);

    void deleteReviews(Long id);

    Review getReviewsById(Long id);

    Collection<Review> getReviewsByFilm(Long id, int count);

    Collection<Review> getAllReviews(int count);

    void likeToReview(Long reviewId, Long userId);

    void dislikeToReview(Long reviewId, Long userId);

    void deleteLike(Long reviewId, Long userId);

    void deleteDislike(Long reviewId, Long userId);
}