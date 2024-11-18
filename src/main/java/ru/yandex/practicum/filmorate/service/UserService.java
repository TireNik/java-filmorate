package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Set;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addFriend (Long id, Long friendId) {
        if (id.equals(friendId)) {
            throw new IllegalArgumentException("Пользователь не может добавить сам себя в друзья.");
        }

        User user = userStorage.getUserById(id);
        User friendsUser = userStorage.getUserById(friendId);

        if (user.getFriends().contains(friendId)) {
            throw new IllegalStateException("Пользователи уже являются друзьями.");
        }

        user.getFriends().add(friendId);
        friendsUser.getFriends().add(id);
        log.debug("Пользователи {} и {} теперь друзья.", id, friendId);
        return user;
    }

    public User deleteFriend (Long id, Long friendId) {
        User user = userStorage.getUserById(id);
        User friendsUser = userStorage.getUserById(friendId);

        if (!user.getFriends().contains(friendId)) {
            throw new IllegalStateException("Пользователи не являются друзьями.");
        }

        user.getFriends().remove(friendId);
        friendsUser.getFriends().remove(id);
        log.debug("Пользователи {} и {} удалены из друзей.", id, friendId);
        return user;
    }

    public Collection<User> getFriends (Long id) {
        return userStorage.getUserById(id).getFriends().stream()
                .map(userStorage::getUserById)
                .toList();
    }

    public Collection<User> getCommonFriends (Long id, Long friendId) {
        Set<Long> user = userStorage.getUserById(id).getFriends();
        Set<Long> friend = userStorage.getUserById(id).getFriends();

        if (user.isEmpty() || friend.isEmpty()) {
            throw new IllegalStateException("У пользователей нет друзей.");
        }

        return user.stream()
                .filter(friend::contains)
                .map(userStorage::getUserById)
                .toList();
    }
}
