package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Builder
public class Feed {

    private Long timestamp;
    private int userId;
    private String eventType;
    private int eventId;
    private String operation;
    private int entityId;

}
