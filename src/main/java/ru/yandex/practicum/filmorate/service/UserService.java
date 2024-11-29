package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PutMapping;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public Collection<User> getUsers() {
        log.info("Получение списка всех пользователей");
        return userStorage.getUsers();
    }

    public User getUserById(Long id) {
        log.info("Получение пользователя {}", id);
        return userStorage.getUserById(id);
    }

    public User addUser(User user) {
        log.info("Добавление пользователя: {}", user);
        User saveUser = userStorage.addUser(user);
        log.info("Пользователь добавлен с ID: {}", user.getId());
        return saveUser;
    }

    @PutMapping
    public User updateUser(User newUser) {
        log.info("Обновление данных пользователя: {}", newUser);
        User oldUser = userStorage.updateUser(newUser);
        log.info("Пользователь с ID {} успешно обновлён", newUser.getId());
        return oldUser;
    }

    public void addFriend(Long id, Long friendId) {
        userStorage.addFriend(id, friendId);
        log.debug("Пользователи {} и {} теперь друзья.", id, friendId);
    }

    public void deleteFriend(Long id, Long friendId) {
        userStorage.deleteFriend(id, friendId);
        log.debug("Пользователи {} и {} удалены из друзей.", id, friendId);
    }

    public List<User> getFriends(Long id) {
        return userStorage.getFriends(id);
    }

    public List<User> getCommonFriends(Long id, Long friendId) {
        return userStorage.getCommonFriends(id, friendId);
    }

    public void confirmFriendship(Long id, Long friendId) {
        userStorage.confirmFriendship(id, friendId);
    }
}
