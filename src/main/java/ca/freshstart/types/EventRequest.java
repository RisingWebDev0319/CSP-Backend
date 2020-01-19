package ca.freshstart.types;

import lombok.Data;

@Data
public class EventRequest {
    private long eventId;
    private long capacity;
}