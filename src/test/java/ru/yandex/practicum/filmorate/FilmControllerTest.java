package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void addFilm_ShouldReturnBadRequest_WhenNameIsEmpty() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\", \"description\":\"A good film.\"," +
                                " \"releaseDate\":\"2000-01-01T00:00:00Z\", \"duration\":\"PT2H\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message").value("Название не может быть пустым"));
    }

    @Test
    public void addFilm_ShouldReturnBadRequest_WhenDescriptionTooLong() throws Exception {
        String longDescription = "A".repeat(201);
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Film\", \"description\":\"" + longDescription + "\"," +
                                " \"releaseDate\":\"2000-01-01T00:00:00Z\", \"duration\":\"PT2H\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message")
                        .value("Максимальная длина описания — 200 символов"));
    }

    @Test
    public void addFilm_ShouldReturnBadRequest_WhenReleaseDateTooEarly() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Old Film\", \"description\":\"An old film.\"," +
                                " \"releaseDate\":\"1890-01-01T00:00:00Z\", \"duration\":\"PT2H\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message")
                        .value("Дата релиза — не раньше 28 декабря 1895 года"));
    }

    @Test
    public void addFilm_ShouldReturnBadRequest_WhenDurationIsNegative() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Film\", \"description\":\"Good film.\"," +
                                " \"releaseDate\":\"2000-01-01T00:00:00Z\", \"duration\":\"PT-1H\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message")
                        .value("Продолжительность фильма должна быть положительным числом"));
    }

    @Test
    public void addFilm_ShouldReturnBadRequest_WhenRequestBodyIsEmpty() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message", containsString("Название не может быть пустым")));
    }
}
