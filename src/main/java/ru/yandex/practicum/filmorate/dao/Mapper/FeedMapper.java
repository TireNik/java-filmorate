package ru.yandex.practicum.filmorate.dao.Mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FeedMapper {

    /*public Feed mapToFeed(ResultSet resultSet) throws SQLException {
        Feed feed = Feed.builder()
                .eventId(resultSet
                        .getInt("event_id"))
                .timestamp(resultSet.getTimestamp("time_event").toInstant().getEpochSecond() * 1000)
                .userId(resultSet.getInt("user_id"))
                .eventType(resultSet.getString("event_type"))
                .operation(resultSet.getString("operation"))
                .entityId(resultSet.getInt("entity_id"))
                .build();

        return feed;
    }*/
    public Feed mapToFeed(ResultSet resultSet) throws SQLException {
        Feed feed = Feed.builder()
                .eventId(resultSet.getInt("event_id"))
                .timestamp(resultSet.getTimestamp("time_event").toInstant().getEpochSecond() * 1000)
                .userId(resultSet.getInt("user_id"))
                .eventType(EventType.valueOf(resultSet.getString("event_type")))
                .operation(Operation.valueOf(resultSet.getString("operation")))
                .entityId(resultSet.getInt("entity_id"))
                .build();

        return feed;
    }



}
