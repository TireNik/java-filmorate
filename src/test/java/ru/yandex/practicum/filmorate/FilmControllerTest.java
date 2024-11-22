package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Film baseFilm;

    @BeforeEach
    public void setup() {
        baseFilm = Film.builder()
                .name("Valid Film")
                .description("A good film.")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();
    }

    @Test
    public void addFilm_ShouldReturnCreated_WhenFilmIsValid() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseFilm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Valid Film"));
    }

    @Test
    public void addFilm_ShouldReturnBadRequest_WhenNameIsEmpty() throws Exception {
        Film filmWithEmptyName = baseFilm.toBuilder().name("").build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filmWithEmptyName)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message").value("Название не может быть пустым"));
    }


    @Test
    public void addFilm_ShouldReturnBadRequest_WhenDescriptionTooLong() throws Exception {
        Film filmWithLongDescription = baseFilm.toBuilder().description("A".repeat(201)).build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filmWithLongDescription)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message").value("Максимальная длина описания — 200 символов"));
    }

    @Test
    public void addFilm_ShouldReturnBadRequest_WhenReleaseDateTooEarly() throws Exception {
        Film filmWithEarlyReleaseDate = baseFilm.toBuilder()
                .releaseDate(LocalDate.of(1895, 12, 27)).build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filmWithEarlyReleaseDate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message")
                        .value("Дата релиза — не раньше 28 декабря 1895 года"));
    }

    @Test
    public void addFilm_ShouldReturnBadRequest_WhenDurationIsNegative() throws Exception {
        Film filmWithNegativeDuration = baseFilm.toBuilder().duration(-120).build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filmWithNegativeDuration)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message")
                        .value("Продолжительность фильма должна быть положительным числом"));
    }

    @Test
    public void updateFilm_ShouldReturnBadRequest_WhenIdIsMissing() throws Exception {
        Film filmWithoutId = baseFilm.toBuilder().id(null).build();

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filmWithoutId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message").value("Id должен быть указан"));
    }

    @Test
    public void updateFilm_ShouldReturnNotFound_WhenFilmNotFound() throws Exception {
        Film nonExistentFilm = baseFilm.toBuilder().id(999L).build();

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nonExistentFilm)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resource not found"))
                .andExpect(jsonPath("$.message").value("Фильм с указанным id не найден"));
    }

    @Test
    public void addLike_ShouldReturnOk_WhenFilmAndUserExist() throws Exception {
        mockMvc.perform(put("/films/1/like/1"))
                .andExpect(status().isOk());
    }

    @Test
    public void addLike_ShouldReturnNotFound_WhenFilmDoesNotExist() throws Exception {
        mockMvc.perform(put("/films/999/like/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resource not found"))
                .andExpect(jsonPath("$.message").value("Фильм с данным id не найден"));
    }
}