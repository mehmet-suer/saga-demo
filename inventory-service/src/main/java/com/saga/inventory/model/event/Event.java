package com.saga.inventory.model.event;

import java.util.UUID;

public interface Event {
    UUID getEventId();

    UUID getKey();
}
