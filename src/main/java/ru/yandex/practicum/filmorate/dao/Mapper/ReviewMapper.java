package ru.yandex.practicum.filmorate.dao.Mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ReviewMapper {
    public Review mapToReview(ResultSet rs) throws SQLException {
        Integer likeId = rs.getInt("like_id");
        Integer dislikeId = rs.getInt("dislike_id");
        Integer useful = likeId - dislikeId;

        return Review.builder()
                .content(rs.getString("content"))
                .isPositive(rs.getBoolean("is_positive"))
                .userId(rs.getLong("user_id"))
                .filmId(rs.getLong("film_id"))
                .useful(useful)
                .build();
    }
}
