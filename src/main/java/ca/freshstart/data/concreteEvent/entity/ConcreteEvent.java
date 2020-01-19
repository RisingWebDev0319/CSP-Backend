package ca.freshstart.data.concreteEvent.entity;

import ca.freshstart.data.client.entity.Client;
import ca.freshstart.data.concreteEvent.interfaces.BaseConcreteEvent;
import ca.freshstart.data.service.entity.Service;
import ca.freshstart.data.session.entity.Session;
import ca.freshstart.types.Duration;
import ca.freshstart.data.concreteEvent.types.ConcreteEventState;
import ca.freshstart.data.event.entity.Event;
import ca.freshstart.data.room.entity.Room;
import ca.freshstart.data.therapist.entity.Therapist;
import ca.freshstart.types.Constants;
import ca.freshstart.helpers.CspDateSerializer;
import ca.freshstart.applications.client.helpers.CspTimeDeserializer;
import ca.freshstart.applications.client.helpers.CspTimeSerializer;
import ca.freshstart.types.EntityId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Concrete event or service on date.
 */
@Entity
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ConcreteEvent extends EntityId implements BaseConcreteEvent {

    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name = "service_id")
    private Service service;// null for event item

    @ManyToOne
    @JoinColumn(name="event_id")
    private Event event;

    @ManyToOne
    @JoinColumn(name="room_id")
    private Room room;

    @ManyToOne
    @JoinColumn(name="therapist_id")
    private Therapist therapist;

    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    private String note;

    @Column(nullable = false)
    @JsonFormat(pattern = Constants.TIME_FORMAT)
    @JsonSerialize(using = CspTimeSerializer.class)
    @JsonDeserialize(using = CspTimeDeserializer.class)
    private Time time;

    @Column(nullable = false)
    @JsonFormat(pattern = Constants.DATE_FORMAT)
    @Temporal(TemporalType.DATE)
    @JsonSerialize(using = CspDateSerializer.class)
    private Date date;

    @Column(nullable = false)
    private ConcreteEventState state;

    /**
     * Can be any value,
     * values defined only in client-side or
     * stored in ca.freshstart.entity.booking.ConcreteEventSubStatus
     */
    private String subStatus;

    @Embedded
    @AttributeOverrides( {
            @AttributeOverride(name="prep", column = @Column(name="time_prep") ),
            @AttributeOverride(name="processing", column = @Column(name="time_processing") ),
            @AttributeOverride(name="clean", column = @Column(name="time_clean") )
    } )
    private Duration duration;

    public Long getRoomId() {
        return room != null ? room.getId() : null;
    }

    public Long getTherapistId() {
        return this.therapist != null ? this.therapist.getId() : null;
    }

    public Service getService() {
        return this.service;
    }

    @Override
    public List<Long> getClientsIds() {
        List<Long> clientsIds = new ArrayList<>();
        clientsIds.add(this.client.getId());
        return clientsIds;
    }

    public Float getSalePrice() {
        return this.service != null ? this.service.getSalePrice() : 0;
    }

    public Float getPurchasePrice() {
        return this.service != null ? this.service.getPurchasePrice() : 0;
    }
}
