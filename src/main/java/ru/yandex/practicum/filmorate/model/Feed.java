package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Feed {

    private int eventId;
    private long timeEvent;
    private int userId;
    private String eventType;
    private String operation;
    private int entityId;

}
