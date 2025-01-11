package ru.yandex.practicum.filmorate.dao.Mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Feed;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FeedMapper {

    public Feed mapToFeed(ResultSet resultSet) throws SQLException {
        return Feed.builder()
                .entityId(resultSet
                        .getInt("event_id"))
                .timeEvent(resultSet.getTimestamp("time_event").toInstant().getEpochSecond())
                .userId(resultSet.getInt("user_id"))
                .eventType(resultSet.getString("event_type"))
                .operation(resultSet.getString("operation"))
                .entityId(resultSet.getInt("entity_id"))
                .build();
    }

}
