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
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
@Slf4j
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbc;
    private final ReviewMapper reviewMapper;

    private static final String INSERT_FEED_QUERY = "INSERT INTO feed (time_event,user_id,event_type," +
            "operation,entity_id) " +
            "VALUES(?,?,?,?,?)";

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
        jdbc.update(INSERT_FEED_QUERY, LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC),
                reviews.getUserId(), EventType.REVIEW.name(), Operation.ADD.name(), reviews.getReviewId());
        return reviews;
    }

    @Override
    public Review updateReviews(Review reviews) {
        final String UPDATE_REVIEW_QUERY = "UPDATE reviews SET content = ?, is_positive = ? " +
                "WHERE review_id = ?";
        int update = jdbc.update(UPDATE_REVIEW_QUERY,
                reviews.getContent(),
                reviews.getIsPositive(),
                reviews.getReviewId()
        );
        if (update == 0) {
            log.error("Не удалось обновить отзыв с id={}", reviews.getReviewId());
            throw new ResourceNotFoundException("Отзыв с указанным id не найден");
        }
        jdbc.update(INSERT_FEED_QUERY, LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC),
                getReviewsById(reviews.getReviewId()).getUserId(), EventType.REVIEW.name(), Operation.UPDATE.name(), reviews.getReviewId());

        return getReviewsById(reviews.getReviewId());
    }

    @Override
    public void deleteReviews(Long id) {
        final String DELETE_USEFUL_QUERY = "DELETE FROM useful WHERE useful_id = ?";
        Long userId = getReviewsById(id).getUserId();
        jdbc.update(DELETE_USEFUL_QUERY, id);

        final String DELETE_REVIEWS_QUERY = "DELETE FROM reviews WHERE review_id = ?";

        jdbc.update(DELETE_REVIEWS_QUERY, id);
        jdbc.update(INSERT_FEED_QUERY, LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC),
                userId, EventType.REVIEW.name(), Operation.REMOVE.name(), id);
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
        List<Review> reviews = jdbc.query(sql, reviewMapper, id, count);
        return reviews.stream().sorted((r1, r2) -> r2.getUseful() - r1.getUseful()).toList();
    }

    @Override
    public List<Review> getAllReviews(int count) {
        final String sql = "SELECT r.REVIEW_ID, r.CONTENT, r.IS_POSITIVE, r.USER_ID, r.FILM_ID, " +
                "COUNT(u.LIKE_ID) AS likes, COUNT(u.DISLIKE_ID) AS dislikes " +
                "FROM REVIEWS r LEFT JOIN USEFUL u ON u.USEFUL_ID = r.REVIEW_ID " +
                "GROUP BY r.REVIEW_ID, r.CONTENT, r.IS_POSITIVE, r.USER_ID, r.FILM_ID LIMIT ?";
        try {
            List<Review> reviews = jdbc.query(sql, reviewMapper, count);
            return reviews.stream().sorted((r1, r2) -> r2.getUseful() - r1.getUseful()).toList();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
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
