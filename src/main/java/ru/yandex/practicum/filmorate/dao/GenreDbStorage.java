package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbc;

    @Override
    public List<Genre> getAllGenres() {
        String sql = "SELECT * FROM genres";
        return jdbc.query(sql, (rs, rowNum) -> rowGenre(rs));
    }

    @Override
    public Optional<Genre> getGenreById(int id) {
        String sql = "SELECT * FROM genres WHERE genre_id = ?";
        final String CHECK_GENRE_ID = "SELECT COUNT(*) FROM genres WHERE genre_id = ?";

        Integer count = jdbc.queryForObject(CHECK_GENRE_ID, Integer.class, id);
        if (count == null || count == 0) {
            log.error("Ошибка получения Genre с id {}", id);
            throw new ResourceNotFoundException("Ошибка получения Genre");
        }

        return jdbc.query(sql, (rs, rowNum) -> rowGenre(rs), id).stream().findFirst();
    }

    private Genre rowGenre(ResultSet rs) throws SQLException {
        int id = rs.getInt("genre_id");
        String name = rs.getString("name");
        return new Genre(id, name);
    }
}
