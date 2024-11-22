package ru.yandex.practicum.filmorate.exception;

public class SelfFriendException extends IllegalArgumentException {
    public SelfFriendException(String message) {
        super(message);
    }
}
