package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

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

}
