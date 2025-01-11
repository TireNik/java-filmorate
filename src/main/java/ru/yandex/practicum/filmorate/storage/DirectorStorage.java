package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;

public interface DirectorStorage {
    List<Director> getAllDirectors();

    Optional<Director> getDirectorById(Long id);

    Director addDirector(Director director);

    Director updateDirector(Director director);

    void deleteDirector(Long id);
}