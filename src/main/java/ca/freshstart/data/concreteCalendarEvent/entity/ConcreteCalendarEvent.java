package ca.freshstart.data.concreteCalendarEvent.entity;

import ca.freshstart.data.calendarEvent.entity.CalendarEvent;
import ca.freshstart.data.concreteEvent.interfaces.BaseCrossEvent;
import ca.freshstart.types.Duration;
import ca.freshstart.data.client.entity.Client;
import ca.freshstart.data.event.entity.Event;
import ca.freshstart.data.room.entity.Room;
import ca.freshstart.data.therapist.entity.Therapist;
import ca.freshstart.types.Constants;
import ca.freshstart.helpers.CspDateSerializer;
import ca.freshstart.applications.client.helpers.CspTimeDeserializer;
import ca.freshstart.applications.client.helpers.CspTimeSerializer;
import ca.freshstart.types.EntityId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.sql.Time;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class ConcreteCalendarEvent extends EntityId implements BaseCrossEvent {

    private String name;

    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name = "calendar_event_id", nullable = false)
    private CalendarEvent calendarEvent;

    @JsonIgnore
    private boolean archived = false;

    @ManyToOne
    @JoinColumn(name="event_id")
    private Event event;

    @ManyToMany(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @JoinTable(name="concrete_calendar_event_clients",
            joinColumns={@JoinColumn(name="concrete_calendar_event_id")},
            inverseJoinColumns={@JoinColumn(name="client_id")})
    private Set<Client> clients;

    @ManyToOne
    @JoinColumn(name="room_id")
    private Room room;

    @ManyToOne
    @JoinColumn(name="therapist_id")
    private Therapist therapist;

    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern = Constants.DATE_FORMAT)
    @JsonSerialize(using = CspDateSerializer.class)
    private Date date;

    @JsonFormat(pattern = Constants.TIME_FORMAT)
    @JsonSerialize(using = CspTimeSerializer.class)
    @JsonDeserialize(using = CspTimeDeserializer.class)
    private Time time;

    @JsonIgnore
    private boolean changed = false;

    @Embedded
    @AttributeOverrides( {
            @AttributeOverride(name="prep", column = @Column(name="time_prep") ),
            @AttributeOverride(name="processing", column = @Column(name="time_processing") ),
            @AttributeOverride(name="clean", column = @Column(name="time_clean") )
    } )
    private Duration duration;

    public ConcreteCalendarEvent() {}

    public ConcreteCalendarEvent(CalendarEvent calendarEvent) {
        this.name = calendarEvent.getName();
        this.event = calendarEvent.getEvent();
        this.room = calendarEvent.getRoom();
        this.therapist = calendarEvent.getTherapist();
        this.time = calendarEvent.getTime();
        this.duration = calendarEvent.getDuration();
        this.calendarEvent = calendarEvent;
    }

    public Set<Client> getClients() {
        Set<Client> clientsCollection = new HashSet<>();
        return (this.clients == null)
                ? clientsCollection
                : this.clients;
    }

    @Override
    public List<Long> getClientsIds() {
        return this.clients.stream().map(Client::getId).collect(Collectors.toList());
    }

    @Transient
    public Float getSalePrice() {
        return this.calendarEvent != null ? this.calendarEvent.getSalePrice() : 0;
    }

    @Transient
    public Float getPurchasePrice() {
        return this.calendarEvent != null ? this.calendarEvent.getPurchasePrice() : 0;
    }

    public CalendarEvent getCalendarEvent() {
        return calendarEvent;
    }

}
