package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserStorage userStorage;
    private final UserService userService;

    @Autowired
    public UserController(UserStorage userStorage, UserService userService) {
        this.userStorage = userStorage;
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> getUsers() {
        log.info("Получение списка всех пользователей");
        return userStorage.getUsers();
    }

    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        log.info("Получение пользователя {}", id);
        return userStorage.getUserById(id);
    }

    @PutMapping("/users/{id}/friends/{friendId}")
    public User addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Добавление друга с id {} пользователю с id {}", friendId, id);
        return userService.addFriend(id, friendId);
    }

    @DeleteMapping("/users/{id}/friends/{friendId}")
    public User deleteFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Удаление друга с id {} пользователю с id {}", friendId, id);
        return userService.deleteFriend(id, friendId);
    }

    @GetMapping("/users/{id}/friends")
    public Collection<User> getFriends(@PathVariable Long id) {
        log.info("Получение всех друзей пользователя {}", id);
        return userService.getFriends(id);
    }

    @GetMapping("/users/{id}/friends/common/{otherId}")
    public Collection<User> getCommonFriends (@PathVariable Long id,
                                              @PathVariable Long otherId) {
        log.info("Получаем общих друзей пользователя {}, с пользователем {} ", id,otherId);
        return userService.getCommonFriends(id, otherId);
    }

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        log.info("Добавление пользователя: {}", user);
        User saveUser = userStorage.addUser(user);
        log.info("Пользователь добавлен с ID: {}", user.getId());
        return saveUser;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User newUser) {
        log.info("Обновление данных пользователя: {}", newUser);
        User oldUser = userStorage.updateUser(newUser);
        log.info("Пользователь с ID {} успешно обновлён", newUser.getId());
        return oldUser;
    }
}
