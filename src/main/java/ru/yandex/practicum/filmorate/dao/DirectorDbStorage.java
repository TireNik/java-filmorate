package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbc;

    @Override
    public List<Director> getAllDirectors() {
        String sql = "SELECT * FROM directors";
        return jdbc.query(sql, (rs, rowNum) -> rowDirector(rs));
    }

    @Override
    public Optional<Director> getDirectorById(Long id) {
        String sql = "SELECT * FROM directors WHERE director_id = ?";
        final String CHECK_DIRECTOR_ID = "SELECT COUNT(*) FROM directors WHERE director_id = ?";

        Integer count = jdbc.queryForObject(CHECK_DIRECTOR_ID, Integer.class, id);
        if (count == null || count == 0) {
            log.error("Ошибка получения Director с id {}", id);
            throw new ResourceNotFoundException("Ошибка получения Director");
        }

        return jdbc.query(sql, (rs, rowNum) -> rowDirector(rs), id).stream().findFirst();
    }

    private Director rowDirector(ResultSet rs) throws SQLException {
        Long id = rs.getLong("director_id");
        String name = rs.getString("name");
        return new Director(id, name);
    }

    @Override
    public Director addDirector(Director director) {
        String sql = "INSERT INTO directors (name) VALUES (?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql, new String[]{"director_id"});
                    ps.setString(1, director.getName());
                    return ps;
                }, keyHolder);
        director.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        if (getDirectorById(director.getId()).isEmpty()) {
            throw new ResourceNotFoundException("Режиссер не найден. Ошибка обновления");
        }

        String sql = "UPDATE directors SET name = ? " +
                "WHERE director_id = ?";
        jdbc.update(sql, director.getName(),
                director.getId());
        return director;
    }

    @Override
    public void deleteDirector(Long directorId) {
        String sql = "DELETE FROM directors WHERE director_id = ?";
        int rowsAffected = jdbc.update(sql, directorId);

        if (rowsAffected == 0) {
            throw new ResourceNotFoundException("Режиссер не найден. Ошибка удаления");
        }
    }
}
