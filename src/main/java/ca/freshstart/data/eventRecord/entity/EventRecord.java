package ca.freshstart.data.eventRecord.entity;

import ca.freshstart.data.event.entity.Event;
import ca.freshstart.types.EntityId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class EventRecord extends EntityId {

    private Long capacity;

    @ManyToOne
    @JoinColumn(name="event_id")
    private Event event;

    public EventRecord() {}

    public EventRecord(long capacity, Event event) {
        this.capacity = capacity;
        this.event = event;
    }
}