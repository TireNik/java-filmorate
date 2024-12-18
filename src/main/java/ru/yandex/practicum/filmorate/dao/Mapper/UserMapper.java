package ru.yandex.practicum.filmorate.dao.Mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

@Component
public class UserMapper {
    public User mapToUser(ResultSet rs, int rowNum) throws SQLException {
        return new User(
                rs.getLong("user_id"),
                rs.getString("email"),
                rs.getString("login"),
                rs.getString("name"),
                rs.getDate("birth_day").toLocalDate(),
                new HashSet<>()
        );
    }

    public void setUserParameters(PreparedStatement ps, User user) throws SQLException {
        ps.setString(1, user.getEmail());
        ps.setString(2, user.getLogin());
        ps.setString(3, user.getName());
        ps.setString(4, String.valueOf(Date.valueOf(user.getBirthday())));
    }
}
