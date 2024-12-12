package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.FriendsShip;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
@Slf4j
@Qualifier("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private static final String INSERT_QUERY = "INSERT INTO users (email, login, name, birth_day) " +
            "VALUES (?, ?, ?, ?)";

    private final JdbcTemplate jdbc;

    @Override
    public User addUser(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, user.getEmail());
                ps.setString(2, user.getLogin());
                ps.setString(3, user.getName());
                ps.setString(4, String.valueOf(Date.valueOf(user.getBirthday())));
                return ps;
            }, keyHolder);

            long userId = keyHolder.getKey().longValue();
            user.setId(userId);
            return user;
        } catch (Exception e) {
            log.error("Неизвестная ошибка при добавлении пользователя: ", e);
            throw new RuntimeException("Неизвестная ошибка при добавлении пользователя", e);
        }
    }

    @Override
    public Collection<User> getUsers() {
        String GET_USERS_QUERY = "SELECT * FROM users";
        return jdbc.query(GET_USERS_QUERY, (rs, rowNum) -> new User(
                rs.getLong("user_id"),
                rs.getString("email"),
                rs.getString("login"),
                rs.getString("name"),
                rs.getDate("birth_day").toLocalDate(),
                new HashSet<>()
        ));
    }

    @Override
    public User getUserById(Long id) {
        String GET_USER_BY_ID_QUERY = "SELECT * FROM users WHERE user_id = ?";
        try {
            return jdbc.queryForObject(GET_USER_BY_ID_QUERY, (rs, rowNum) -> new User(
                    rs.getLong("user_id"),
                    rs.getString("email"),
                    rs.getString("login"),
                    rs.getString("name"),
                    rs.getDate("birth_day").toLocalDate(),
                    new HashSet<>()
            ), id);
        } catch (EmptyResultDataAccessException e) {
            log.error("Пользователь с id={} не найден", id);
            throw new RuntimeException("Пользователь с указанным id не найден", e);
        }
    }

    @Override
    public User updateUser(User newUser) {
        String UPDATE_USER_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birth_day = ? WHERE user_id = ?";
        int rowsUpdated = jdbc.update(UPDATE_USER_QUERY,
                newUser.getEmail(),
                newUser.getLogin(),
                newUser.getName(),
                Date.valueOf(newUser.getBirthday()),
                newUser.getId()
        );
        if (rowsUpdated == 0) {
            log.error("Не удалось обновить пользователя с id={}", newUser.getId());
            throw new UserNotFoundException("Пользователь с указанным id не найден");
        }
        return getUserById(newUser.getId());
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        final String CHECK_QUERY = "SELECT * FROM friendships WHERE user_id = ? AND friend_id = ?";
        final String INSERT_QUERY = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, ?)";
        final String UPDATE_QUERY = "UPDATE friendships SET status = TRUE WHERE user_id = ? AND friend_id = ?";

        try {
            List<FriendsShip> existingFriendship = jdbc.query(CHECK_QUERY,
                    (rs, rowNum) -> new FriendsShip(
                            rs.getLong("user_id"),
                            rs.getLong("friend_id"),
                            rs.getBoolean("status")
                    ),
                    userId, friendId
            );

            if (existingFriendship.isEmpty()) {
                jdbc.update(INSERT_QUERY, userId, friendId, false);
            } else if (!existingFriendship.get(0).isStatus()) {
                jdbc.update(UPDATE_QUERY, userId, friendId);
            }

            List<FriendsShip> reverseFriendship = jdbc.query(CHECK_QUERY,
                    (rs, rowNum) -> new FriendsShip(
                            rs.getLong("user_id"),
                            rs.getLong("friend_id"),
                            rs.getBoolean("status")
                    ),
                    friendId, userId
            );

            if (!reverseFriendship.isEmpty() && !reverseFriendship.get(0).isStatus()) {
                jdbc.update(UPDATE_QUERY, friendId, userId);
            }
        } catch (Exception e) {
            log.error("Ошибка при добавлении друга: userId={}, friendId={}", userId, friendId, e);
            throw new RuntimeException("Ошибка при добавлении друга", e);
        }
    }

    @Override
    public void deleteFriend(Long id, Long friendId) {
        String DELETE_FRIEND_QUERY = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        int rowsDeleted = jdbc.update(DELETE_FRIEND_QUERY, id, friendId);
        if (rowsDeleted == 0) {
            log.warn("Не удалось удалить друга с id={} у пользователя с id={}", friendId, id);
        } else {
            log.info("Друг с id={} успешно удален у пользователя с id={}", friendId, id);
        }
    }

    @Override
    public List<User> getFriends(Long id) {
        final String GET_FRIENDS_QUERY =
                "SELECT u.* FROM users u " +
                        "JOIN friendships f ON u.user_id = f.friend_id " +
                        "WHERE f.user_id = ?";

        return jdbc.query(GET_FRIENDS_QUERY,
                (rs, rowNum) -> new User(
                        rs.getLong("user_id"),
                        rs.getString("email"),
                        rs.getString("login"),
                        rs.getString("name"),
                        rs.getDate("birth_day").toLocalDate(),
                        new HashSet<>()
                ),
                id
        );
    }

    @Override
    public List<User> getCommonFriends(Long id, Long friendId) {
        String GET_COMMON_FRIENDS_QUERY =
                "SELECT u.* FROM users u " +
                        "JOIN friendships f1 ON u.user_id = f1.friend_id " +
                        "JOIN friendships f2 ON u.user_id = f2.friend_id " +
                        "WHERE f1.user_id = ? AND f2.user_id = ? AND f1.status = TRUE AND f2.status = TRUE";
        return jdbc.query(GET_COMMON_FRIENDS_QUERY,
                (rs, rowNum) -> new User(
                        rs.getLong("user_id"),
                        rs.getString("email"),
                        rs.getString("login"),
                        rs.getString("name"),
                        rs.getDate("birth_day").toLocalDate(),
                        new HashSet<>()
                ),
                id, friendId
        );
    }
}
