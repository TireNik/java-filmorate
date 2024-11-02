package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private String createTestUserJson(String email, String login, String name, String birthday) {
        return String.format("{\"email\":\"%s\", \"login\":\"%s\", \"name\":\"%s\", \"birthday\":\"%s\"}",
                email, login, name, birthday);
    }

    @Test
    public void addUser_ShouldFailOnEmptyRequest() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addUser_ShouldFailOnEmptyEmail() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createTestUserJson("", "testUser", "Test User",
                                "2000-01-01")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",
                        containsString("Электронная почта не может быть пустой")));
    }

    @Test
    public void addUser_ShouldFailOnInvalidEmail() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createTestUserJson("invalidEmail", "testUser", "Test User",
                                "2000-01-01")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",
                        containsString("Электронная почта должна содержать символ @")));
    }

    @Test
    public void addUser_ShouldFailOnFutureBirthday() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createTestUserJson("test@example.com", "testUser",
                                "Test User", "2099-01-01")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",
                        containsString("Дата рождения не может быть в будущем")));
    }

    @Test
    public void addUser_ShouldReturnUserWithId() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createTestUserJson("test@example.com", "testUser",
                                "Test User", "2000-01-01")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.login").value("testUser"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.birthday").value("2000-01-01"));
    }

    @Test
    public void getUsers_ShouldReturnListOfUsers() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createTestUserJson("test2@example.com", "testUser2", "Test User 2",
                                "1999-01-01")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].email", is("test2@example.com")));
    }

    @Test
    public void updateUser_ShouldReturnUpdatedUser() throws Exception {
        // Сначала создаем пользователя
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createTestUserJson("test@example.com", "testUser", "Test User",
                                "2000-01-01")))
                .andExpect(status().isOk());

        // Обновляем пользователя
        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1, \"email\":\"updated@example.com\", \"login\":\"updatedUser\"," +
                                " \"name\":\"Updated User\", \"birthday\":\"1995-01-01\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.login").value("updatedUser"))
                .andExpect(jsonPath("$.name").value("Updated User"))
                .andExpect(jsonPath("$.birthday").value("1995-01-01"));
    }
}
