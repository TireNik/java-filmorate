package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import static org.junit.matchers.JUnitMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private String createTestFilmJson(String name, String description, String releaseDate, int duration) {
        return String.format("{\"name\":\"%s\", \"description\":\"%s\", \"releaseDate\":\"%s\", \"duration\":%d}",
                name, description, releaseDate, duration);
    }

    @Test
    public void addFilm_ShouldFailOnEmptyName() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createTestFilmJson("", "A valid description", "2000-01-01", 120)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Validation error")))
                .andExpect(jsonPath("$.message", containsString("Название не может быть пустым")));
    }

    @Test
    public void addFilm_ShouldFailOnInvalidReleaseDate() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createTestFilmJson("Valid Name", "A valid description", "1800-01-01", 120)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Validation error")))
                .andExpect(jsonPath("$.message", containsString("Дата релиза — не раньше 28 декабря 1895 года")));
    }

    @Test
    public void addFilm_ShouldFailOnNegativeDuration() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createTestFilmJson("Valid Name", "A valid description", "2000-01-01", -10)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Validation error")))
                .andExpect(jsonPath("$.message", containsString("Продолжительность фильма должна быть положительным числом")));
    }

//    @Test
//    public void addFilm_ShouldPassOnValidInput() throws Exception {
//        mockMvc.perform(post("/films")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(createTestFilmJson("Valid Name", "A valid description", "2000-01-01", 120)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.name").value("Valid Name"))
//                .andExpect(jsonPath("$.description").value("A valid description"))
//                .andExpect(jsonPath("$.releaseDate").value("2000-01-01"))
//                .andExpect(jsonPath("$.duration").value(120));
//    }
}