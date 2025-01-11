package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.Mapper.UserMapper;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Repository
@Slf4j
@RequiredArgsConstructor
@Qualifier("friendDbStorage")
public class FriendDbStorage implements FriendshipStorage {
    private final JdbcTemplate jdbc;
    private final UserMapper userMapper;

    private static final  String INSERT_FEED_QUERY = "INSERT INTO feed (time_event,user_id," +
            "event_type,operation,entity_id) " +
            "VALUES(?,?,'FRIEND',?,?)";


    @Override
    public void addFriend(Long userId, Long friendId) {
        final String CHECK_USER_QUERY = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        final String CHECK_QUERY = "SELECT COUNT(*) FROM friendship WHERE (user_id = ? AND friend_id = ?) " +
                "OR (user_id = ? AND friend_id = ?)";
        final String INSERT_QUERY = "INSERT INTO friendship (user_id, friend_id, status) VALUES (?, ?, ?)";
        final String UPDATE_QUERY = "UPDATE friendship SET status = TRUE WHERE user_id = ? AND friend_id = ?";
        final String UPDATE_REVERSE_QUERY = "UPDATE friendship SET status = FALSE WHERE user_id = ? AND friend_id = ?";


        try {
            Integer userCount = jdbc.queryForObject(CHECK_USER_QUERY, Integer.class, userId);
            if (userCount == null || userCount == 0) {
                throw new UserNotFoundException("Пользователь с ID " + userId + " не найден");
            }

            Integer friendCount = jdbc.queryForObject(CHECK_USER_QUERY, Integer.class, friendId);
            if (friendCount == null || friendCount == 0) {
                throw new UserNotFoundException("Пользователь с ID " + friendId + " не найден");
            }

            int count = jdbc.queryForObject(CHECK_QUERY, Integer.class, userId, friendId, friendId, userId);

            if (count > 0) {
                return;
            }

            jdbc.update(INSERT_QUERY, userId, friendId, false);

            count = jdbc.queryForObject(CHECK_QUERY, Integer.class, friendId, userId, userId, friendId);

            if (count > 0) {
                jdbc.update(UPDATE_QUERY, userId, friendId);
                jdbc.update(UPDATE_REVERSE_QUERY, friendId, userId);
                jdbc.update(INSERT_FEED_QUERY, LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC),
                        userId, "ADD", friendId);
            }
        } catch (UserNotFoundException e) {
            log.error("Ошибка: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Ошибка при добавлении друга: userId={}, friendId={}", userId, friendId, e);
            throw new RuntimeException("Ошибка при добавлении друга", e);
        }
    }

    @Override
    public void deleteFriend(Long id, Long friendId) {
        final String DELETE_FRIEND_QUERY = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
        final String CHECK_USER_QUERY = "SELECT COUNT(*) FROM users WHERE user_id = ?";

        try {
            Integer userCount = jdbc.queryForObject(CHECK_USER_QUERY, Integer.class, id);
            if (userCount == null || userCount == 0) {
                throw new UserNotFoundException("Пользователь с ID " + id + " не найден");
            }
            Integer friendCount = jdbc.queryForObject(CHECK_USER_QUERY, Integer.class, friendId);
            if (friendCount == null || friendCount == 0) {
                throw new UserNotFoundException("Пользователь с ID " + friendId + " не найден");
            }
            jdbc.update(DELETE_FRIEND_QUERY, id, friendId);
            jdbc.update(INSERT_FEED_QUERY, LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC),
                    id, "REMOVE", friendId);
        } catch (UserNotFoundException e) {
            log.error("Ошибка: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public List<User> getFriends(Long id) {
        final String GET_FRIENDS_QUERY =
                "SELECT u.* FROM users u " +
                        "JOIN friendship f ON u.user_id = f.friend_id " +
                        "WHERE f.user_id = ?";
        final String CHECK_USER_QUERY = "SELECT COUNT(*) FROM users WHERE user_id = ?";

        try {
            Integer userCount = jdbc.queryForObject(CHECK_USER_QUERY, Integer.class, id);
            if (userCount == null || userCount == 0) {
                throw new UserNotFoundException("Пользователь с ID " + id + " не найден");
            }
            return jdbc.query(GET_FRIENDS_QUERY, userMapper::mapToUser, id);
        } catch (UserNotFoundException e) {
            log.error("Ошибка: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public List<User> getCommonFriends(Long id, Long friendId) {
        final String GET_COMMON_FRIENDS_QUERY =
                "SELECT u.* FROM users u " +
                        "JOIN friendship f1 ON u.user_id = f1.friend_id " +
                        "JOIN friendship f2 ON u.user_id = f2.friend_id " +
                        "WHERE f1.user_id = ? AND f2.user_id = ?";
        return jdbc.query(GET_COMMON_FRIENDS_QUERY, userMapper::mapToUser, id, friendId);
    }
}
