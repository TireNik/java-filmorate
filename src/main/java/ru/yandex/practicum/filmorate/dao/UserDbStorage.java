package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ValidationException;
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
    public void addFriend(Long id, Long friendId) {

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
