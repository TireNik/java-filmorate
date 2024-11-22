package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.SelfFriendException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(Long id, Long friendId) {

        if (id == null || friendId == null) {
            throw new IllegalArgumentException("ID пользователя или друга не может быть null.");
        }

        if (id.equals(friendId)) {
            throw new SelfFriendException("Пользователь не может добавить сам себя в друзья.");
        }

        User user = userStorage.getUserById(id);
        if (user == null) {
            throw new UserNotFoundException("Пользователь с ID " + id + " не найден.");
        }

        User friendsUser = userStorage.getUserById(friendId);
        if (friendsUser == null) {
            throw new UserNotFoundException("Пользователь с ID " + friendId + " не найден.");
        }

        if (user.getFriends().contains(friendId)) {
            throw new IllegalStateException("Пользователи уже являются друзьями.");
        }

        user.getFriends().add(friendId);
        friendsUser.getFriends().add(id);
        log.debug("Пользователи {} и {} теперь друзья.", id, friendId);
    }

    public User deleteFriend(Long id, Long friendId) {
        User user = userStorage.getUserById(id);
        User friendsUser = userStorage.getUserById(friendId);

        user.getFriends().remove(friendId);
        friendsUser.getFriends().remove(id);
        log.debug("Пользователи {} и {} удалены из друзей.", id, friendId);
        return user;
    }

    public Set<User> getFriends(Long id) {
        User user = userStorage.getUserById(id);
        if (user == null) {
            throw new UserNotFoundException("Пользователь с ID " + id + " не найден.");
        }
        return user.getFriends().stream()
                .map(userStorage::getUserById)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public Collection<User> getCommonFriends(Long id, Long friendId) {
        Set<Long> user = userStorage.getUserById(id).getFriends();
        Set<Long> friend = userStorage.getUserById(friendId).getFriends();

        if (user.isEmpty() || friend.isEmpty()) {
            throw new IllegalStateException("У пользователей нет друзей.");
        }

        return user.stream()
                .filter(friend::contains)
                .map(userStorage::getUserById)
                .toList();
    }
}
