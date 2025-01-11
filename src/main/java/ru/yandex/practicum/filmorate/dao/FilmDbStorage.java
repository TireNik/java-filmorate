package ru.yandex.practicum.filmorate.dao;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.Mapper.FilmMapper;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Qualifier("filmDbStorage")
@AllArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;
    private final FilmMapper filmMapper;

    @Override
    public Film getFilmById(Long id) {
        final String sql = "SELECT f.film_id, f.name, f.description, f.releaseDate, f.duration, r.rating_id, r.name AS rating_name " +
                "FROM films AS f " +
                "LEFT JOIN mpa_rating AS r ON f.rating_id = r.rating_id " +
                "WHERE f.film_id = ?";

        return jdbc.query(sql, rs -> {
            if (rs.next()) {
                Film film = filmMapper.mapToFilm(rs);
                film.getGenres().addAll(getGenresByFilmId(id));
                return film;
            }
            return null;
        }, id);
    }

    private Set<Genre> getGenresByFilmId(Long filmId) {
        String sql = "SELECT g.genre_id, g.name " +
                "FROM film_genres AS fg " +
                "JOIN genres AS g ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id = ?";
        return new LinkedHashSet<>(Objects.requireNonNull(jdbc.query(sql, filmMapper::mapToGenres, filmId)));
    }

    private Set<Director> getDirectorsByFilmId(Long filmId) {
        String sql = "SELECT d.director_id, d.name " +
                "FROM directors_films AS df " +
                "JOIN directors AS d ON df.director_id = d.director_id " +
                "WHERE df.film_id = ?";
        return new LinkedHashSet<>(Objects.requireNonNull(jdbc.query(sql, filmMapper::mapToDirectors, filmId)));
    }

    @Override
    public Collection<Film> getFilms() {
        String sqlFilms = "SELECT f.film_id, f.name, f.description, f.releaseDate, f.duration, r.rating_id, " +
                "r.name AS rating_name " +
                "FROM films AS f " +
                "LEFT JOIN mpa_rating AS r ON f.rating_id = r.rating_id";

        String sqlGenres = "SELECT fg.film_id, g.genre_id, g.name " +
                "FROM film_genres AS fg " +
                "JOIN genres AS g ON fg.genre_id = g.genre_id";

        String sqlDirectors = "SELECT df.film_id, d.director_id, d.name " +
                "FROM directors_films AS df " +
                "JOIN directors AS d ON df.director_id = d.director_id";

        List<Film> films = jdbc.query(sqlFilms, (rs, rowNum) -> filmMapper.mapToFilm(rs));

        Map<Long, Set<Genre>> filmGenresMap = jdbc.query(sqlGenres, rs -> {
            Map<Long, Set<Genre>> map = new HashMap<>();
            while (rs.next()) {
                long filmId = rs.getLong("film_id");
                Genre genre = new Genre(rs.getInt("genre_id"), rs.getString("name"));
                map.computeIfAbsent(filmId, k -> new HashSet<>()).add(genre);
            }
            return map;
        });

        Map<Long, Set<Director>> filmDirectorMap = jdbc.query(sqlDirectors, rs -> {
            Map<Long, Set<Director>> map = new HashMap<>();
            while (rs.next()) {
                long filmId = rs.getLong("film_id");
                Director director = new Director(rs.getLong("director_id"), rs.getString("name"));
                map.computeIfAbsent(filmId, k -> new HashSet<>()).add(director);
            }
            return map;
        });

        films.forEach(film -> {
            assert filmGenresMap != null;
            film.setGenres(filmGenresMap.getOrDefault(film.getId(), Collections.emptySet()));
            assert filmDirectorMap != null;
            film.setDirectors(filmDirectorMap.getOrDefault(film.getId(), Collections.emptySet()));
        });

        return films;
    }

    @Override
    public Film addFilm(Film film) {
        validateRatingExists(film.getMpa().getId());
        validateGenresExist(film.getGenres());

        String sql = "INSERT INTO films (name, description, releaseDate, duration, rating_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql, new String[]{"film_id"});
                    ps.setString(1, film.getName());
                    ps.setString(2, film.getDescription());
                    ps.setDate(3, Date.valueOf(film.getReleaseDate()));
                    ps.setInt(4, film.getDuration());
                    ps.setInt(5, film.getMpa().getId());
                    return ps;
                }, keyHolder);
        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        addGenresToFilm(film);
        return film;
    }

    private void validateRatingExists(int ratingId) {
        String sql = "SELECT COUNT(*) FROM mpa_rating WHERE rating_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, ratingId);
        if (count == null || count == 0) {
            throw new ValidationException("Рейтинг с ID " + ratingId + " не найден.");
        }
    }

    private void validateGenresExist(Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }
        String sql = "SELECT COUNT(*) FROM genres WHERE genre_id = ?";
        for (Genre genre : genres) {
            Integer count = jdbc.queryForObject(sql, Integer.class, genre.getId());
            if (count == null || count == 0) {
                throw new ValidationException("Жанр с ID " + genre.getId() + " не найден.");
            }
        }
    }

    private void addGenresToFilm(Film film) {
        String delSql = "DELETE FROM film_genres WHERE film_id = ?";
        LinkedHashSet<Genre> genres = film.getGenres();
        Long id = film.getId();
        jdbc.update(delSql, id);

        if (genres == null || genres.isEmpty()) {
            return;
        }

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
        validateRatingExists(film.getMpa().getId());
        validateGenresExist(film.getGenres());

        if (getFilmById(film.getId()) == null) {
            throw new ResourceNotFoundException("Фильм не найден. Ошибка обнавления");
        }

        String sql = "UPDATE films SET name = ?, description = ?, releaseDate = ?, duration = ?, rating_id = ? " +
                "WHERE film_id = ?";
        jdbc.update(sql, film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
        addGenresToFilm(film);
        return film;
    }

    @Override
    public List<Film> getPopularFilms(int count, Integer genreId, Integer year) {
        String insertion = "";
        if (genreId == null && year != null) {
            insertion = "WHERE YEAR(f.releaseDate)=" + year;
        }
        if (genreId != null && year == null) {
            insertion = "LEFT JOIN FILM_GENRES AS fg ON fg.FILM_ID = f.FILM_ID WHERE fg.GENRE_ID =" + genreId;
        }
        if (genreId != null && year != null) {
            insertion = "LEFT JOIN FILM_GENRES AS fg ON fg.FILM_ID = f.FILM_ID WHERE fg.GENRE_ID =" + genreId +
                    " AND YEAR(f.releaseDate)=" + year;
        }

        String sql = "SELECT f.film_id, f.name, f.description, f.releaseDate, f.duration, r.rating_id, " +
                "r.name AS rating_name, COUNT(l.user_id) AS likes " +
                "FROM films AS f " +
                "LEFT JOIN likes AS l ON f.film_id = l.film_id " +
                "LEFT JOIN mpa_rating AS r ON f.rating_id = r.rating_id " +
                insertion +
                "GROUP BY f.film_id, r.rating_id, r.name " +
                "ORDER BY likes DESC " +
                "LIMIT ?";
        List<Film> films = jdbc.query(sql, (rs, rowNum) -> filmMapper.mapToFilm(rs), count);

        if (films.isEmpty()) {
            return films;
        }

        List<Long> filmsIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());
        addGenresToFilms(filmsIds, films);
        return films;
    }

    @Override
    public List<Film> getPopularCommonFilms(Long userId, Long friendId) {
        String sql = "SELECT FILM_ID FROM LIKES WHERE USER_ID = ? OR USER_ID = ? GROUP BY FILM_ID HAVING COUNT(*)>1";
        List<Long> filmsIds = jdbc.query(sql, rs -> {
            List<Long> ids = new ArrayList<>();
            while (rs.next()) {
                ids.add(rs.getLong("FILM_ID"));
            }
            return ids;
        }, userId, friendId);
        assert filmsIds != null;
        if (filmsIds.isEmpty()) {
            return new ArrayList<>();
        }
        String inSql = filmsIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
        sql = "SELECT f.film_id, f.name, f.description, f.releaseDate, f.duration, r.rating_id, " +
                "r.name AS rating_name, COUNT(l.user_id) AS likes " +
                "FROM films AS f " +
                "LEFT JOIN likes AS l ON f.film_id = l.film_id " +
                "LEFT JOIN mpa_rating AS r ON f.rating_id = r.rating_id " +
                "WHERE f.film_id IN (?)" +
                "GROUP BY f.film_id, r.rating_id, r.name " +
                "ORDER BY likes DESC ";
        List<Film> films = jdbc.query(sql, (rs, rowNum) -> filmMapper.mapToFilm(rs), inSql);

        addGenresToFilms(filmsIds, films);
        return films;
    }

    private void addGenresToFilms(List<Long> ids, List<Film> films) {

        String inSql = ids.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));

        String genresByFilmsSql = "SELECT fg.film_id, g.genre_id, g.name " +
                "FROM film_genres AS fg " +
                "JOIN genres AS g ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id IN (" + inSql + ")";


        Map<Long, Set<Genre>> genresByFilmId = jdbc.query(genresByFilmsSql, rs -> {
            Map<Long, Set<Genre>> genreMap = new HashMap<>();
            while (rs.next()) {
                long filmId = rs.getLong("film_id");
                Genre genre = new Genre(rs.getInt("genre_id"), rs.getString("name"));
                genreMap.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(genre);
            }
            return genreMap;
        });
        films.forEach(film -> film.setGenres(genresByFilmId.getOrDefault(film.getId(), new LinkedHashSet<>())));

    }


}