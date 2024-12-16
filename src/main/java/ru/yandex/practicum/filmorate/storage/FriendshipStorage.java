package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface FriendshipStorage {

    void addFriend(Long id, Long friendId);

    void deleteFriend(Long id, Long friendId);

    List<User> getFriends(Long id);

    List<User> getCommonFriends(Long id, Long friendId);
}
