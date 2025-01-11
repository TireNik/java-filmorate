package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class DirectorService {
    private final DirectorStorage directorStorage;

    public DirectorService(DirectorStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

    public List<Director> getAllDirectors() {
        log.info("Получение списка всех режиссеров");
        return directorStorage.getAllDirectors();
    }

    public Optional<Director> getDirectorById(Long id) {
        log.info("Получение режиссера по ID: {}", id);
        return directorStorage.getDirectorById(id);
    }

    public Director addDirector(Director director) {
        log.info("Добавление режиссера: {}", director.getName());
        return directorStorage.addDirector(director);
    }

    public Director updateDirector(Director director) {
        directorStorage.getDirectorById(director.getId());
        log.info("Обновление режиссера: {}", director.getName());
        return directorStorage.updateDirector(director);
    }

    public void deleteDirector(Long id) {
        log.info("Удаление режиссера c ID: {}", id);
        directorStorage.deleteDirector(id);
    }
}
