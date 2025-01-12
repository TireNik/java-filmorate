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

        try {
            log.info("Попытка получение фильма с ID {}", id);

            Film film = jdbc.queryForObject(sql, (rs, rowNum) -> filmMapper.mapToFilm(rs), id);
            Set<Genre> genres = getGenresByFilmId(id);
            film.setGenres(genres);
            Set<Director> directors = getDirectorsByFilmId(id);
            film.setDirectors(directors);
            return film;
        } catch (Exception e) {
            log.info("Ошибка получения фильма по причине {}", e.getMessage());
            throw new ResourceNotFoundException("Фильм не найден");
        }
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
    public List<Film> getFilmsByDirector(long directorId, String sortBy) {
        String sqlFilms = "SELECT f.film_id, f.name, f.description, f.releaseDate, f.duration, r.rating_id, " +
                "r.name AS rating_name, COUNT(l.user_id) AS likes_count " +
                "FROM films AS f " +
                "JOIN directors_films df ON f.film_id = df.film_id " +
                "LEFT JOIN mpa_rating AS r ON f.rating_id = r.rating_id " +
                "LEFT JOIN likes l ON f.film_id = l.film_id " +
                "WHERE df.director_id = ? " +
                "GROUP BY f.film_id, r.rating_id " +
                "ORDER BY " + (sortBy.equals("likes") ? "likes_count DESC" : "f.releaseDate");

        String sqlGenres = "SELECT fg.film_id, g.genre_id, g.name " +
                "FROM film_genres AS fg " +
                "JOIN genres AS g ON fg.genre_id = g.genre_id";

        String sqlDirectors = "SELECT df.film_id, d.director_id, d.name " +
                "FROM directors_films AS df " +
                "JOIN directors AS d ON df.director_id = d.director_id";

        List<Film> films = jdbc.query(sqlFilms, new Object[]{directorId}, (rs, rowNum) -> filmMapper.mapToFilm(rs));

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
        validateDirectorsExist(film.getDirectors());

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
        addDirectorsToFilm(film);
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

    private void validateDirectorsExist(Set<Director> directors) {
        if (directors == null || directors.isEmpty()) {
            return;
        }
        String sql = "SELECT COUNT(*) FROM directors WHERE director_id = ?";
        for (Director director : directors) {
            Integer count = jdbc.queryForObject(sql, Integer.class, director.getId());
            if (count == null || count == 0) {
                throw new ValidationException("Режиссер с ID " + director.getId() + " не найден.");
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

    private void addDirectorsToFilm(Film film) {
        String delSql = "DELETE FROM directors_films WHERE film_id = ?";
        LinkedHashSet<Director> directors = film.getDirectors();
        Long id = film.getId();
        jdbc.update(delSql, id);

        if (directors == null || directors.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO directors_films(film_id, director_id) VALUES(?, ?)";
        jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Director director = (Director) directors.toArray()[i];
                ps.setLong(1, id);
                ps.setLong(2, director.getId());
            }

            @Override
            public int getBatchSize() {
                return directors.size();
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
        addDirectorsToFilm(film);
        return film;
    }

    @Override
    public List<Film> getPopularFilms(int count, Integer genreId, Integer year) {
        String insertion = "";
        if (genreId == null && year != null) {
            insertion = "WHERE YEAR(f.releaseDate) = " + year;
        }
        if (genreId != null && year == null) {
            insertion = "LEFT JOIN film_genres AS fg ON fg.film_id = f.film_id WHERE fg.genre_id = " + genreId;
        }
        if (genreId != null && year != null) {
            insertion = "LEFT JOIN film_genres AS fg ON fg.film_id = f.film_id WHERE fg.genre_id = " + genreId + " AND YEAR(f.releaseDate) = " + year;
        }

        String sql = "SELECT f.film_id, f.name, f.description, f.releaseDate, f.duration, r.rating_id, " +
                "r.name AS rating_name, COUNT(l.user_id) AS likes " +
                "FROM films AS f " +
                "LEFT JOIN likes AS l ON f.film_id = l.film_id " +
                "LEFT JOIN mpa_rating AS r ON f.rating_id = r.rating_id " +
                insertion +
                " GROUP BY f.film_id, r.rating_id, r.name " +
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

        String directorsByFilmsSql = "SELECT df.film_id, d.director_id, d.name " +
                "FROM directors_films AS df " +
                "JOIN directors AS d ON df.director_id = d.director_id " +
                "WHERE df.film_id IN (" + inSql + ")";

        Map<Long, Set<Genre>> genresByFilmId = jdbc.query(genresByFilmsSql, rs -> {
            Map<Long, Set<Genre>> genreMap = new HashMap<>();
            while (rs.next()) {
                long filmId = rs.getLong("film_id");
                Genre genre = new Genre(rs.getInt("genre_id"), rs.getString("name"));
                genreMap.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(genre);
            }
            return genreMap;
        });

        Map<Long, Set<Director>> directorsByFilmId = jdbc.query(directorsByFilmsSql, rs -> {
            Map<Long, Set<Director>> directorMap = new HashMap<>();
            while (rs.next()) {
                long filmId = rs.getLong("film_id");
                Director director = new Director(rs.getLong("director_id"), rs.getString("name"));
                directorMap.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(director);
            }
            return directorMap;
        });

        films.forEach(film -> {
            assert genresByFilmId != null;
            film.setGenres(genresByFilmId.getOrDefault(film.getId(), new LinkedHashSet<>()));
        });
        films.forEach(film -> {
            assert directorsByFilmId != null;
            film.setDirectors(directorsByFilmId.getOrDefault(film.getId(), new LinkedHashSet<>()));
        });
    }

    @Override
    public void deleteFilm(Long id) {
        String deleteReviewsSql = "DELETE FROM reviews WHERE film_id = ?";
        String deleteLikesSql = "DELETE FROM likes WHERE film_id = ?";
        String deleteFilmGenresSql = "DELETE FROM film_genres WHERE film_id = ?";
        String deleteUsefulSql = "DELETE FROM useful WHERE useful_id = ?";

        jdbc.update(deleteReviewsSql, id);
        jdbc.update(deleteLikesSql, id);
        jdbc.update(deleteFilmGenresSql, id);
        jdbc.update(deleteUsefulSql, id);

        String deleteFilmSql = "DELETE FROM films WHERE film_id = ?";
        jdbc.update(deleteFilmSql, id);

        log.info("Фильм с id {} был успешно удален", id);
    }

    public List<Film> searchFilmsTitleAndDirector(String queryStr) {
        String sql = "SELECT f.*, m.name AS rating_name, g.genre_id, g.name AS genre_name, d.director_id, d.name AS director_name " +
                "FROM films f " +
                "JOIN mpa_rating m ON f.rating_id = m.rating_id " +
                "LEFT JOIN film_genres fg ON f.film_id = fg.film_id " +
                "LEFT JOIN genres g ON fg.genre_id = g.genre_id " +
                "LEFT JOIN directors_films df ON f.film_id = df.film_id " +
                "LEFT JOIN directors d ON df.director_id = d.director_id " +
                "WHERE LOWER(f.name) LIKE ? OR LOWER(d.name) LIKE ?";

        List<Film> films = jdbc.query(sql, (rs, rowNum) -> {
            Film film = filmMapper.mapToFilm(rs);
            Integer genreId = rs.getInt("genre_id");
            String genreName = rs.getString("genre_name");
            addGenreToFilm(film, genreId, genreName);

            Long directorId = rs.getLong("director_id");
            String directorName = rs.getString("director_name");
            addDirectorToFilm(film, directorId, directorName);

            return film;
        }, "%" + queryStr.toLowerCase() + "%", "%" + queryStr.toLowerCase() + "%");

        Map<Long, Film> filmMap = new HashMap<>();
        for (Film film : films) {
            filmMap.put(film.getId(), film);
        }

        List<Film> uniqueFilms = new ArrayList<>(filmMap.values());
        log.debug("Получены все Film по названию и режиссёру {}", queryStr);

        return uniqueFilms;
    }

    private void addDirectorToFilm(Film film, Long directorId, String directorName) {
        if (directorName != null) {
            Director director = new Director(directorId, directorName);
            film.getDirectors().add(director);
        }
    }

    public List<Film> searchFilmsTitle(String queryStr) {
        String sql = "SELECT f.*, m.name AS rating_name, g.genre_id, g.name AS genre_name\n" +
                "FROM films f\n" +
                "JOIN mpa_rating m ON f.rating_id = m.rating_id\n" +
                "LEFT JOIN film_genres fg ON f.film_id = fg.film_id\n" +
                "LEFT JOIN genres g ON fg.genre_id = g.genre_id\n" +
                "WHERE LOWER(f.name) LIKE ?";

        List<Film> films = jdbc.query(sql, (rs, rowNum) -> {
            Film film = filmMapper.mapToFilm(rs);
            Integer genreId = rs.getInt("genre_id");
            String genreName = rs.getString("genre_name");
            addGenreToFilm(film, genreId, genreName);
            return film;
        }, "%" + queryStr.toLowerCase() + "%");

        Map<Long, Film> filmMap = new HashMap<>();
        for (Film film : films) {
            filmMap.put(film.getId(), film);
        }

        List<Film> uniqueFilms = new ArrayList<>(filmMap.values());
        log.debug("Получены все фильмы по названию {}", queryStr);

        return uniqueFilms;
    }


    private void addGenreToFilm(Film film, Integer genreId, String genreName) {
        if (genreName != null) {
            Genre genre = new Genre(genreId, genreName);
            film.getGenres().add(genre);
        }
    }

    public List<Film> searchFilmsDirector(String queryStr) {
        String sql = "SELECT f.*, m.name AS rating_name, g.genre_id, g.name AS genre_name " +
                "FROM films f " +
                "JOIN directors_films df ON df.film_id = f.film_id " +
                "JOIN directors d ON d.director_id = df.director_id " +
                "LEFT JOIN mpa_rating m ON f.rating_id = m.rating_id " +
                "LEFT JOIN film_genres fg ON f.film_id = fg.film_id " +
                "LEFT JOIN genres g ON fg.genre_id = g.genre_id " +
                "WHERE LOWER(d.name) LIKE ?";

        List<Film> films = jdbc.query(sql, (rs, rowNum) -> {
            Film film = filmMapper.mapToFilm(rs);
            Integer genreId = rs.getInt("genre_id");
            String genreName = rs.getString("genre_name");
            addGenreToFilm(film, genreId, genreName);
            return film;
        }, "%" + queryStr.toLowerCase() + "%");

        Map<Long, Film> filmMap = new HashMap<>();
        for (Film film : films) {
            filmMap.put(film.getId(), film);
        }

        List<Film> uniqueFilms = new ArrayList<>(filmMap.values());

        for (Film film : uniqueFilms) {
            addDirectorsForFilm(film);
        }

        log.debug("Получены все фильмы по имени режиссёра {}", queryStr);
        return uniqueFilms;
    }


    private void addDirectorsForFilm(Film film) {
        if (film == null) {
            return;
        }
        List<Director> directors = fetchDirectorsByFilmId(film.getId());
        film.setDirectors(directors);
    }

    private List<Director> fetchDirectorsByFilmId(Long filmId) {
        String sql = "SELECT d.* FROM directors d " +
                "JOIN directors_films df ON d.director_id = df.director_id " +
                "WHERE df.film_id = ?";

        return jdbc.query(sql, (rs, rowNum) -> {
            return new Director(
                    rs.getLong("director_id"),
                    rs.getString("name")
            );
        }, filmId);
    }
}