package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/directors")
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    public List<Director> getAllDirectors() {
        log.info("GET / directors");
        return directorService.getAllDirectors();
    }

    @GetMapping("/{id}")
    public Optional<Director> getDirectorById(@PathVariable("id") Long id) {
        log.info("GET / directors / {}", id);
        return directorService.getDirectorById(id);
    }

    @PostMapping
    public Director addDirector(@Valid @RequestBody Director director) {
        log.info("Добавляем режиссера: {}", director);
        return directorService.addDirector(director);
    }

    @PutMapping
    public Director updateDirector(@Valid @RequestBody Director newDirector) {
        log.info("Обновляем режиссера с ID: {}", newDirector.getId());
        return directorService.updateDirector(newDirector);
    }

    @DeleteMapping("/{id}")
    public void deleteDirector(@PathVariable("id") Long id) {
        log.info("Удаляем режиссера с ID: {}", id);
        directorService.deleteDirector(id);
    }
}
