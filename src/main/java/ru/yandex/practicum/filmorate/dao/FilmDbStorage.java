package ru.yandex.practicum.filmorate.dao;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.*;
import java.sql.Date;
import java.util.*;

@Slf4j
@Repository
@Qualifier("filmDbStorage")
@AllArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;

    @Override
    public Film getFilmById(Long id) {
        String sql = "SELECT f.film_id, f.name, f.description, f.releaseDate, f.duration, r.rating_id, r.name AS rating_name " +
                "FROM films AS f " +
                "LEFT JOIN mpa_rating AS r ON f.rating_id = r.rating_id " +
                "WHERE f.film_id = ?";

        return jdbc.query(sql, rs -> {
            if (rs.next()) {
                Film film = Film.builder()
                        .id(rs.getLong("film_id"))
                        .name(rs.getString("name"))
                        .description(rs.getString("description"))
                        .releaseDate(rs.getDate("releaseDate").toLocalDate())
                        .duration(rs.getInt("duration"))
                        .mpa(new Mpa(rs.getInt("rating_id"), rs.getString("rating_name")))
                        .build();
                film.getGenres().addAll(getGenresByFilmId(id));
                return film;
            }
            return null; // Или выбросить исключение, если фильм не найден
        }, id);
    }

    private Set<Genre> getGenresByFilmId(Long filmId) {
        String sql = "SELECT g.genre_id, g.name " +
                "FROM film_genres AS fg " +
                "JOIN genres AS g ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id = ?";
        return new LinkedHashSet<>(jdbc.query(sql, (rs, rowNum) ->
                new Genre(rs.getInt("genre_id"), rs.getString("name")), filmId));
    }

    @Override
    public Collection<Film> getFilms() {
        String sql = "SELECT f.film_id, f.name, f.description, f.releaseDate, f.duration, r.rating_id, r.name AS rating_name " +
                "FROM films AS f " +
                "LEFT JOIN mpa_rating AS r ON f.rating_id = r.rating_id";

        List<Film> films = jdbc.query(sql, (rs, rowNum) -> {
            Film film = Film.builder()
                    .id(rs.getLong("film_id"))
                    .name(rs.getString("name"))
                    .description(rs.getString("description"))
                    .releaseDate(rs.getDate("releaseDate").toLocalDate())
                    .duration(rs.getInt("duration"))
                    .mpa(new Mpa(rs.getInt("rating_id"), rs.getString("rating_name")))
                    .build();
            film.getGenres().addAll(getGenresByFilmId(film.getId()));
            return film;
        });
        return films;
    }

    @Override
    public Film addFilm(Film film) {
        String sql = "INSERT INTO films (name, description, releaseDate, duration, rating_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql, new String[]{"film_id"});
                    ps.setString(1, film.getName());
                    log.info("jdbcUPDATE NAME");
                    ps.setString(2, film.getDescription());
                    ps.setDate(3, Date.valueOf(film.getReleaseDate()));
                    ps.setInt(4, film.getDuration());
                    ps.setInt(5, film.getMpa().getId());
                    log.info("jdbcUPDATE MPA");
                    return ps;
                }, keyHolder);
        film.setId(keyHolder.getKey().longValue());
        log.info("jdbcUPDATE setId");
        log.info("jdbcUPDATE updateGenres");
        addGenresToFilm(film);
        return film;
    }

    private void addGenresToFilm(Film film) {
        String delSql = "DELETE FROM film_genres WHERE film_id = ?";
        LinkedHashSet<Genre> genres = film.getGenres();
        Long id = film.getId();
        jdbc.update(delSql, id);

        String sql = "INSERT INTO film_genres(film_id, genre_id) VALUES(?, ?)";
        jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Genre genre = (Genre) genres.toArray()[i];
                ps.setLong(1, id);
                ps.setInt(2, genre.getId());
            }

            @Override
            public int getBatchSize() {
                return genres.size();
            }
        });
    }

    @Override
    public Film updateFilm(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, releaseDate = ?, duration = ?, rating_id = ? " +
                "WHERE film_id = ?";
        jdbc.update(sql, film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
        return film;
    }

    @Override
    public void addLike(Film film, User user) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbc.update(sql, film.getId(), user.getId());
        log.info("Лайк добавлен фильму с id {} от пользователя с id {}", film.getId(), user.getId());
    }

    @Override
    public void deleteLike(Film film, User user) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbc.update(sql, film.getId(), user.getId());
        log.info("Лайк удален у фильма с id {} от пользователя с id {}", film.getId(), user.getId());
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        String sql = "SELECT f.film_id, f.name, f.description, f.releaseDate, f.duration, r.rating_id, r.name AS rating_name, COUNT(l.user_id) AS likes " +
                "FROM films AS f " +
                "LEFT JOIN likes AS l ON f.film_id = l.film_id " +
                "LEFT JOIN mpa_rating AS r ON f.rating_id = r.rating_id " +
                "GROUP BY f.film_id, r.rating_id, r.name " +
                "ORDER BY likes DESC " +
                "LIMIT ?";
        return jdbc.query(sql, (rs, rowNum) -> {
            Film film = Film.builder()
                    .id(rs.getLong("film_id"))
                    .name(rs.getString("name"))
                    .description(rs.getString("description"))
                    .releaseDate(rs.getDate("releaseDate").toLocalDate())
                    .duration(rs.getInt("duration"))
                    .mpa(new Mpa(rs.getInt("rating_id"), rs.getString("rating_name")))
                    .build();
            film.getGenres().addAll(getGenresByFilmId(film.getId()));
            return film;
        }, count);
    }


}