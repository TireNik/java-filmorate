package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;

@Data
@Builder
public class Feed {

    private Long timestamp;
    private int userId;
    private EventType eventType;
    private int eventId;
    private Operation operation;
    private int entityId;

}
