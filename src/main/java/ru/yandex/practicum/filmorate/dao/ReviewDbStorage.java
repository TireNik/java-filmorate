package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.Mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
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
    private final ReviewMapper reviewMapper;

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
        reviews.setReviewId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return reviews;
    }

    @Override
    public Review updateReviews(Review reviews) {
        String UPDATE_REVIEW_QUERY = "UPDATE reviews SET content = ?, is_positive = ?, film_id = ? " +
                "WHERE review_id = ?";
        int update = jdbc.update(UPDATE_REVIEW_QUERY,
                reviews.getContent(),
                reviews.getIsPositive(),
                reviews.getFilmId(),
                reviews.getReviewId()
        );
        if (update == 0) {
            log.error("Не удалось обновить отзыв с id={}", reviews.getReviewId());
            throw new ResourceNotFoundException("Отзыв с указанным id не найден");
        }
        return reviews;
    }

    @Override
    public void deleteReviews(Long id) {
        String DELETE_QUERY = "DELETE FROM reviews WHERE review_id = ?";
        jdbc.update(DELETE_QUERY, id);
    }

    @Override
    public Review getReviewsById(Long id) {
        String QUERY = "SELECT r.content, r.is_positive, u.name AS user_name, f.name AS film_name, " +
                "(SELECT COUNT(*) FROM useful WHERE useful_id = r.review_id AND like_id IS NOT NULL) AS likes, " +
                "(SELECT COUNT(*) FROM useful WHERE useful_id = r.review_id AND  dislike_id IS NOT NULL) AS dislikes " +
                "FROM reviews r " +
                "JOIN users u ON r.user_id = u.user_id " +
                "JOIN films f ON r.film_id = f.film_id " +
                "WHERE r.review_id = ?";
        return jdbc.queryForObject(QUERY, (rs, rowNum) -> reviewMapper.mapToReview(rs), id);
    }

    @Override
    public List<Review> getReviewsByFilm(Long id) {

        return List.of();
    }

    @Override
    public void likeToReview(Long reviewId, Long userId) {
        String sql = "INSERT INTO useful (useful_id, like_id, dislike_id) VALUES (?, ?, NULL)";
        log.info("Like insert");
        jdbc.update(sql, reviewId, userId);
        log.info("Successful insert");
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
