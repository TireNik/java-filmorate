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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
@Slf4j
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbc;
    private final ReviewMapper reviewMapper;

    @Override
    public Review addReviews(Review reviews) {
        final String INSERT_QUERY = "INSERT INTO reviews (content, is_positive, user_id, film_id) " +
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
        final String UPDATE_REVIEW_QUERY = "UPDATE reviews SET content = ?, is_positive = ?, film_id = ? " +
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
        return getReviewsById(reviews.getReviewId());
    }

    @Override
    public void deleteReviews(Long id) {
        final String DELETE_USEFUL_QUERY = "DELETE FROM useful WHERE useful_id = ?";
        jdbc.update(DELETE_USEFUL_QUERY, id);

        final String DELETE_REVIEWS_QUERY = "DELETE FROM reviews WHERE review_id = ?";
        jdbc.update(DELETE_REVIEWS_QUERY, id);
    }

    @Override
    public Review getReviewsById(Long id) {
        final String QUERY = "SELECT r.review_id, r.content, r.is_positive, u.name AS user_name, f.name AS film_name, " +
                "r.user_id, r.film_id, " +
                "COALESCE(SUM(CASE WHEN uf.like_id IS NOT NULL THEN 1 ELSE 0 END), 0) AS likes, " +
                "COALESCE(SUM(CASE WHEN uf.dislike_id IS NOT NULL THEN 1 ELSE 0 END), 0) AS dislikes " +
                "FROM reviews r " +
                "JOIN users u ON r.user_id = u.user_id " +
                "JOIN films f ON r.film_id = f.film_id " +
                "LEFT JOIN useful uf ON r.review_id = uf.useful_id " +
                "WHERE r.review_id = ? " +
                "GROUP BY r.review_id, r.content, r.is_positive, u.name, f.name, r.user_id, r.film_id";

        Review review = jdbc.queryForObject(QUERY, reviewMapper, id);
        log.info("get review");
        return review;
    }

    @Override
    public List<Review> getReviewsByFilm(Long id, int count) {
        final String sql = "SELECT r.review_id, r.content, r.is_positive, u.name AS user_name, f.name AS film_name, " +
                "r.user_id, r.film_id, " +
                "COALESCE(SUM(CASE WHEN uf.like_id IS NOT NULL THEN 1 ELSE 0 END), 0) AS likes, " +
                "COALESCE(SUM(CASE WHEN uf.dislike_id IS NOT NULL THEN 1 ELSE 0 END), 0) AS dislikes " +
                "FROM reviews r " +
                "JOIN users u ON r.user_id = u.user_id " +
                "JOIN films f ON r.film_id = f.film_id " +
                "LEFT JOIN useful uf ON r.review_id = uf.useful_id " +
                "WHERE r.film_id = ? " +
                "GROUP BY r.review_id, r.content, r.is_positive, u.name, f.name, r.user_id, r.film_id " +
                "LIMIT ?";
        return jdbc.query(sql, reviewMapper, id, count);
    }

    @Override
    public List<Review> getAllReviews(int count) {
        final String sql = "SELECT * FROM reviews LIMIT ?";
        return jdbc.query(sql, reviewMapper, count);
    }

    @Override
    public void likeToReview(Long reviewId, Long userId) {
        String checkSql = "SELECT * FROM useful WHERE useful_id = ? AND like_id = ? OR useful_id = ? AND dislike_id = ?";
        List<Map<String, Object>> existingLikeDislike = jdbc.queryForList(checkSql, reviewId, userId, reviewId, userId);

        if (!existingLikeDislike.isEmpty()) {
            String updateSql;
            if (existingLikeDislike.get(0).get("like_id") != null) {
                updateSql = "UPDATE useful SET like_id = NULL, dislike_id = ? WHERE useful_id = ? AND like_id = ?";
                jdbc.update(updateSql, userId, reviewId, userId);
            } else {
                updateSql = "UPDATE useful SET dislike_id = NULL, like_id = ? WHERE useful_id = ? AND dislike_id = ?";
                jdbc.update(updateSql, userId, reviewId, userId);
            }
        } else {
            String insertSql = "INSERT INTO useful (useful_id, like_id, dislike_id) VALUES (?, ?, NULL)";
            jdbc.update(insertSql, reviewId, userId);
        }
        log.info("Лайк для отзыва {} добавлен или обновлен для пользователя {}", reviewId, userId);
    }

    @Override
    public void dislikeToReview(Long reviewId, Long userId) {
        String checkSql = "SELECT * FROM useful WHERE useful_id = ? AND like_id = ? OR useful_id = ? AND dislike_id = ?";
        List<Map<String, Object>> existingLikeDislike = jdbc.queryForList(checkSql, reviewId, userId, reviewId, userId);

        if (!existingLikeDislike.isEmpty()) {
            String updateSql;
            if (existingLikeDislike.get(0).get("dislike_id") != null) {
                updateSql = "UPDATE useful SET dislike_id = NULL, like_id = ? WHERE useful_id = ? AND dislike_id = ?";
                jdbc.update(updateSql, userId, reviewId, userId);
            } else {
                updateSql = "UPDATE useful SET like_id = NULL, dislike_id = ? WHERE useful_id = ? AND like_id = ?";
                jdbc.update(updateSql, userId, reviewId, userId);
            }
        } else {
            String insertSql = "INSERT INTO useful (useful_id, like_id, dislike_id) VALUES (?, NULL, ?)";
            jdbc.update(insertSql, reviewId, userId);
        }
        log.info("Дизлайк для отзыва {} добавлен или обновлен для пользователя {}", reviewId, userId);
    }

    @Override
    public void deleteLike(Long reviewId, Long userId) {
        final String sql = "DELETE FROM useful WHERE useful_id = ? AND like_id = ?";
        log.info("Удаление лайка");
        jdbc.update(sql, reviewId, userId);
        log.info("Лайк удален");
    }

    @Override
    public void deleteDislike(Long reviewId, Long userId) {
        final String sql = "DELETE FROM useful WHERE useful_id = ? AND dislike_id = ?";
        log.info("Удаление дислака");
        jdbc.update(sql, reviewId, userId);
        log.info("Дислайк удален");
    }
}
