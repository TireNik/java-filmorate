package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getUsers() {
        log.info("Получение списка всех пользователей");
        return users.values();
    }

    @PostMapping
    public User addUser(@RequestBody User user) {
        log.info("Добавление пользователя: {}", user);
        validation(user);

        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Имя пользователя пустое, используем логин в качестве имени: {}", user.getLogin());
            user.setName(user.getLogin());
        }

        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь добавлен с ID: {}", user.getId());
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User newUser) {
        log.info("Обновление данных пользователя: {}", newUser);

        if (newUser.getId() == null) {
            log.error("Не указан ID пользователя при обновлении");
            throw new ValidationException("Id должен быть указан");
        }
        if (!users.containsKey(newUser.getId())) {
            log.error("Пользователь с ID {} не найден", newUser.getId());
            throw new ValidationException("Пользователь с указанным id не найден");
        }

        validation(newUser);

        User oldUser = users.get(newUser.getId());
        oldUser.setName(newUser.getName());
        oldUser.setBirthday(newUser.getBirthday());
        oldUser.setEmail(newUser.getEmail());
        oldUser.setLogin(newUser.getLogin());

        log.info("Пользователь с ID {} успешно обновлён", newUser.getId());
        return oldUser;
    }

    private void validation(User user) {
        log.info("Валидация пользователя: {}", user);

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.error("Электронная почта пользователя не может быть пустой");
            throw new ValidationException("Электронная почта не может быть пустой");
        }

        if (!user.getEmail().contains("@")) {
            log.error("Электронная почта {} должна содержать символ '@'", user.getEmail());
            throw new ValidationException("Электронная почта должна содержать символ @");
        }

        if (user.getBirthday().isAfter(Instant.from(LocalDate.now()))) {
            log.error("Дата рождения {} находится в будущем", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

        log.info("Валидация пользователя пройдена успешно: {}", user);
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
