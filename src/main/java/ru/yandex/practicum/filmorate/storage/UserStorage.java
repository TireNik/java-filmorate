package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;


public interface UserStorage {
    Collection<User> getUsers();

    User getUserById(Long id);

    User addUser(User user);

    User updateUser(User newUser);

    void addFriend(Long id, Long friendId);

    void deleteFriend(Long id, Long friendId);

    List<User> getFriends(Long id);

    List<User> getCommonFriends(Long id, Long friendId);

    void confirmFriendship(Long id, Long friendId);
}
