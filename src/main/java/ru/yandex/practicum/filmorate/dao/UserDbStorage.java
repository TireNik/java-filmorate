package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.FriendsShip;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;

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
        return List.of();
    }

    @Override
    public User getUserById(Long id) {
        return null;
    }



    @Override
    public User updateUser(User newUser) {
        return null;
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        final String CHECK_QUERY = "SELECT * FROM friendships WHERE user_id = ? AND friend_id = ?";
        final String INSERT_QUERY = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, ?)";
        final String UPDATE_QUERY = "UPDATE friendship SET status = ? WHERE user_id = ? AND fried_id = ?";

        try {
            List<FriendsShip> friendsShips = jdbc.query(CHECK_QUERY, (rs, rowNum) ->
                    new FriendsShip(
                            rs.getLong("user_id"),
                            rs.getLong("friend_id"),
                            rs.getBoolean("status")
                    ), userId, friendId
            );

            if (friendsShips.isEmpty()) {
                jdbc.update(INSERT_QUERY, userId, friendId, false);
            } else {
                jdbc.update(UPDATE_QUERY,true, userId, friendId);
                jdbc.update(UPDATE_QUERY, true, friendId,userId);
            }
        } catch (Exception e) {
            log.error("Ошибка при добавлении друга: userId={}, friendId={}", userId, friendId, e);
            throw new RuntimeException("Ошибка при добавлении друга", e);
        }
    }

    @Override
    public void deleteFriend(Long id, Long friendId) {

    }

    @Override
    public List<User> getFriends(Long id) {
        return List.of();
    }

    @Override
    public List<User> getCommonFriends(Long id, Long friendId) {
        return List.of();
    }
}
