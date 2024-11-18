package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage{

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> getUser() {
        return users.values();
    }

    @Override
    public User addUser(User user) {
        nameValid(user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.debug("Пользователь добавлен в хранилище: ID = {}, Name = {}", user.getId(), user.getName());
        return user;
    }

    @Override
    public User updateUser(User newUser) {
        nameValid(newUser);
        if (newUser.getId() == null) {
            log.error("Не указан ID пользователя при обновлении");
            throw new ValidationException("Id должен быть указан");
        }
        if (!users.containsKey(newUser.getId())) {
            log.error("Пользователь с ID {} не найден", newUser.getId());
            throw new ResourceNotFoundException("Пользователь с указанным id не найден");
        }

        User oldUser = users.get(newUser.getId());
        oldUser.setName(newUser.getName());
        oldUser.setBirthday(newUser.getBirthday());
        oldUser.setEmail(newUser.getEmail());
        oldUser.setLogin(newUser.getLogin());
        log.debug("Пользователь обнавлен в хранилище: ID = {}, Name = {}", oldUser.getId(), oldUser.getName());

        return oldUser;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void nameValid(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Имя пользователя пустое, используем логин в качестве имени: {}", user.getLogin());
            user.setName(user.getLogin());
        }
    }
}