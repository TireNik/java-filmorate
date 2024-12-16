package ru.yandex.practicum.filmorate.dao.Mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class FilmMapper {
    public Film mapToFilm(ResultSet rs) throws SQLException {
        return Film.builder()
                .id(rs.getLong("film_id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("releaseDate").toLocalDate())
                .duration(rs.getInt("duration"))
                .mpa(new Mpa(rs.getInt("rating_id"), rs.getString("rating_name")))
                .build();
    }

    public Set<Genre> mapToGenres(ResultSet rs) throws SQLException {
        Set<Genre> genres = new LinkedHashSet<>();
        while (rs.next()) {
            Genre genre = new Genre(rs.getInt("genre_id"), rs.getString("name"));
            genres.add(genre);
        }
        return genres;
    }
}
