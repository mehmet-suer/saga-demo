package com.saga.payment.model.event;

import java.util.UUID;

public interface Event {
    UUID getEventId();
    UUID getKey();

}
