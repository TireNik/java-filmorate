package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.Mapper.FeedMapper;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.storage.FeedStorage;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Qualifier("feedDbStorage")
public class FeedDbStorage implements FeedStorage {

    private final JdbcTemplate jdbc;
    private final FeedMapper mapper;

    private static final String GET_FEED_BY_ID_QUERY ="SELECT * FROM feed WHERE user_id = ?";


    @Override
    public List<Feed> getFeed(Long id) {
            return jdbc.query(GET_FEED_BY_ID_QUERY, (rs, rowNum) -> mapper.mapToFeed(rs),id);
    }
}
