package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.LikeStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
@Qualifier("likeDbStorage")
public class LikeDbStorage implements LikeStorage {
    private final JdbcTemplate jdbc;

    private final static String INSERT_FEED_QUERY = "INSERT INTO feed (time_event,user_id,event_type,operation,entity_id) " +
            "VALUES(?,?,'LIKE',?,?)";

    @Override
    public void addLike(Film film, User user) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbc.update(sql, film.getId(), user.getId());
        jdbc.update(INSERT_FEED_QUERY, LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC),
                user.getId(),"ADD",film.getId());
        log.info("Лайк добавлен фильму с id {} от пользователя с id {}", film.getId(), user.getId());
    }

    @Override
    public void deleteLike(Film film, User user) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbc.update(sql, film.getId(), user.getId());
        jdbc.update(INSERT_FEED_QUERY, LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC),
                user.getId(),"REMOVE",film.getId());
        log.info("Лайк удален у фильма с id {} от пользователя с id {}", film.getId(), user.getId());
    }

    @Override
    public List<Long> getFriendsOfInterestDB(Long userId) {
        final String sql = "SELECT fl.user_id, COUNT(fl.film_id) AS rate FROM likes ul " +
                "JOIN likes fl ON (ul.film_id = fl.film_id AND ul.user_id != fl.user_id) " +
                "JOIN users u ON (fl.user_id != u.user_id) " +
                "WHERE ul.user_id = ? " +
                "GROUP BY fl.user_id " +
                "HAVING rate > 1 " +
                "ORDER BY rate DESC " +
                "LIMIT 10";

        List<Long> usersIdsSameLeads = jdbc.query(sql, (rs, rowNum) -> rs.getLong("user_id"), userId);
        log.debug("Получаем список id пользователей с пересекающимися лайками для пользователя {}.", userId);
        return usersIdsSameLeads;
    }

    @Override
    public List<Long> getRecommendedFilmsDB(Long userId, List<Long> friendsOfInterestIds) {
        String userIdParams = String.join(",", Collections.nCopies(friendsOfInterestIds.size(), "?"));
        final String sql = "SELECT fl.film_id FROM likes fl " +
                "WHERE fl.user_id IN (" + userIdParams + ") " +
                "AND fl.film_id NOT IN (SELECT ul.film_id FROM likes ul WHERE ul.user_id = ?)";

        List<Long> recommendedFilmsIds = jdbc.query(sql, LikeDbStorage::mapRow, friendsOfInterestIds.toArray(), userId);
        log.debug("Получаем список id фильмов рекомендованных для пользователя {}.", userId);
        return recommendedFilmsIds;
    }

    private static Long mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getLong("film_id");
    }
}
