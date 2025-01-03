package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {
    Review addReviews (Review reviews);

    Review updateReviews (Review reviews);

    void deleteReviews (Long id);

    Review getReviewsById (Long id);

    List<Review> getReviewsByFilm (Long id);

    void likeToReview (Long id);

    void dislikeToReview (Long id);

    void deleteLike (Long id);

    void deleteDislike (Long id);
}
