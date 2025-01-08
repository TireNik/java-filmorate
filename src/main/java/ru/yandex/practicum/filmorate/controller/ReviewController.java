package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/films/reviews")
@Slf4j
@Validated
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public Review addReviews (@Validated @RequestBody Review reviews) {
        log.info("Пытаюсь добавить отзыв");
        return reviewService.addReviews(reviews);
    }

    @PutMapping
    public Review updateReview(@Validated @RequestBody Review review) {
        log.info("Попытка обновления отзыва");
        return reviewService.updateReviews(review);
    }

    @GetMapping("/{id}")
    public Review getReviewById (@PathVariable Long id) {
        return reviewService.getReviewsById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Long id) {
        reviewService.deleteReviews(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void likeToReview(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.likeToReview(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void dislikeToReview(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.dislikeToReview(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.deleteLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.deleteDislike(id, userId);
    }

    @GetMapping
    public Collection<Review> getReviewsByFilm(@RequestParam(required = false) Long filmId,
                                               @RequestParam(defaultValue = "10") int count) {
        if (filmId != null) {
            return reviewService.getReviewsByFilm(filmId, count);
        } else {
            return reviewService.getAllReviews(count);
        }
    }
}
