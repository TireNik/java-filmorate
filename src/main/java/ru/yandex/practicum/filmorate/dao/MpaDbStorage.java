package ru.yandex.practicum.filmorate.dao;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@AllArgsConstructor
public class MpaDbStorage implements MpaStorage {
    JdbcTemplate jdbc;

    @Override
    public List<Mpa> getAllMpa() {
        String sql = "SELECT * FROM mpa_rating";
        List<Mpa> allMpa;
        try {
            allMpa = jdbc.query(sql, (rs, rowNum) -> rowMpa(rs));
            log.info("Mpa найдены");
        } catch (DataAccessException e) {
            log.error("Error getAllMpa");
            throw new RuntimeException(e);
        }
        return allMpa;
    }

    @Override
    public Optional<Mpa> getMpaById(Integer id) {
        String sql = "SELECT * FROM mpa_rating WHERE rating_id = ?";
        final String CHECK_MPA_ID = "SELECT COUNT(*) FROM mpa_rating WHERE rating_id = ?";

        Integer count = jdbc.queryForObject(CHECK_MPA_ID, Integer.class, id);
        if (count == null || count == 0) {
            log.error("Ошибка получения Mpa с id {}", id);
            throw new ResourceNotFoundException("Ошибка получения Mpa");
        }

            return jdbc.query(sql, (rs, rowNum) -> rowMpa(rs), id)
                    .stream()
                    .findFirst();
    }

    private Mpa rowMpa(ResultSet rs) throws SQLException {
        int mpaId = rs.getInt("rating_id");
        String name = rs.getString("name");
        return new Mpa(mpaId, name);
    }
}
