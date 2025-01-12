package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.Mapper.UserMapper;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;

@Repository
@Slf4j
@Qualifier("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private static final String INSERT_QUERY = "INSERT INTO users (email, login, name, birth_day) " +
            "VALUES (?, ?, ?, ?)";

    private final JdbcTemplate jdbc;
    private final UserMapper userMapper;

    @Override
    public User addUser(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
                userMapper.setUserParameters(ps, user);
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
        final String GET_USERS_QUERY = "SELECT * FROM users";
        return jdbc.query(GET_USERS_QUERY, userMapper::mapToUser);
    }

    @Override
    public User getUserById(Long id) {
        final String GET_USER_BY_ID_QUERY = "SELECT * FROM users WHERE user_id = ?";
        try {
            return jdbc.queryForObject(GET_USER_BY_ID_QUERY, userMapper::mapToUser, id);
        } catch (EmptyResultDataAccessException e) {
            log.error("Пользователь с id={} не найден", id);
            throw new UserNotFoundException("Пользователь с указанным id не найден");
        }
    }

    @Override
    public User updateUser(User newUser) {
        final String UPDATE_USER_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birth_day = ? WHERE user_id = ?";
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
    public void deleteUser(Long id) {
        String deleteFriendshipsSql = "DELETE FROM friendship WHERE user_id = ? OR friend_id = ?";
        String deleteLikesSql = "DELETE FROM likes WHERE user_id = ?";
        String deleteReviewsSql = "DELETE FROM reviews WHERE user_id = ?";
        String deleteUsefulSql = "DELETE FROM useful WHERE like_id = ? OR dislike_id = ?";
        String deleteFeedSql = "DELETE FROM feed WHERE user_id = ?";

        jdbc.update(deleteFriendshipsSql, id, id);
        jdbc.update(deleteLikesSql, id);
        jdbc.update(deleteReviewsSql, id);
        jdbc.update(deleteUsefulSql, id, id);

        String deleteUserSql = "DELETE FROM users WHERE user_id = ?";
        jdbc.update(deleteUserSql, id);
        jdbc.update(deleteFeedSql,id);

        log.info("Пользователь с id {} был успешно удален", id);
    }
}
