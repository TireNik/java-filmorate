package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

@Repository
@Slf4j
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbc;

    @Override
    public Review addReviews(Review reviews) {
        String INSERT_QUERY = "INSERT INTO reviews (content, is_positive, user_id, film_id) " +
                "VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, reviews.getContent());
            ps.setBoolean(2, reviews.getIsPositive());
            ps.setLong(3, reviews.getUserId());
            ps.setLong(4, reviews.getFilmId());
            return ps;
        }, keyHolder);
        reviews.builder().reviewId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return reviews;
    }

    @Override
    public Review updateReviews(Review reviews) {
        return null;
    }

    @Override
    public void deleteReviews(Review reviews) {

    }

    @Override
    public Review getReviewsById(Review reviews) {
        return null;
    }

    @Override
    public List<Review> getReviewsByFilm(Long id) {
        return List.of();
    }

    @Override
    public void likeToReview(Long id) {

    }

    @Override
    public void dislikeToReview(Long id) {

    }

    @Override
    public void deleteLike(Long id) {

    }

    @Override
    public void deleteDislike(Long id) {

    }
}
