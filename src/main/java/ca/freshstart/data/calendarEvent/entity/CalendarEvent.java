package ca.freshstart.data.calendarEvent.entity;

import ca.freshstart.helpers.CspDateSerializer;
import ca.freshstart.applications.client.helpers.CspTimeDeserializer;
import ca.freshstart.applications.client.helpers.CspTimeSerializer;
import ca.freshstart.data.equipment.entity.Equipment;
import ca.freshstart.data.event.entity.Event;
import ca.freshstart.data.eventRecord.entity.EventRecord;
import ca.freshstart.data.room.entity.Room;
import ca.freshstart.data.therapist.entity.Therapist;
import ca.freshstart.types.Constants;
import ca.freshstart.types.Duration;
import ca.freshstart.helpers.*;
import ca.freshstart.types.EntityId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class CalendarEvent extends EntityId {

    @Column(unique = true)
    private String name;

    private Long capacity;

    private boolean archived = false;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "therapist_id")
    private Therapist therapist;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "calendar_event_equipments",
            joinColumns = {@JoinColumn(name = "calendar_event_id")},
            inverseJoinColumns = {@JoinColumn(name = "equipment_id")})
    private Set<Equipment> equipment;

    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern = Constants.DATE_FORMAT)
    @JsonSerialize(using = CspDateSerializer.class)
    private Date dateStart;

    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern = Constants.DATE_FORMAT)
    @JsonSerialize(using = CspDateSerializer.class)
    private Date dateEnd;

    // days of week
    // from 0 to 6. 0 -> Sunday
    private int[] days;

    @JsonFormat(pattern = Constants.TIME_FORMAT)
    @JsonSerialize(using = CspTimeSerializer.class)
    @JsonDeserialize(using = CspTimeDeserializer.class)
    private Time time;

    @JsonSerialize(using = JsonDateSerializer.class)
    public Date getDateStart() {
        return dateStart;
    }

    @JsonSerialize(using = JsonDateSerializer.class)
    public Date getDateEnd() {
        return dateEnd;
    }

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "prep", column = @Column(name = "time_prep")),
            @AttributeOverride(name = "processing", column = @Column(name = "time_processing")),
            @AttributeOverride(name = "clean", column = @Column(name = "time_clean"))
    })
    private Duration duration;

    public Long getMinimalCapacity() {
        ArrayList<Long> capacityList = new ArrayList<>();
        capacityList.add(this.capacity);

        Room room = this.getRoom();
        if (room != null) {
            capacityList.add(room.getCapacity());
        }

        Therapist therapist = this.getTherapist();
        if (therapist != null) {
            Long eventId = this.getEvent().getId();
            therapist.getEvents().stream()
                    .filter((EventRecord eventRecord) -> eventRecord.getEvent().getId().equals(eventId))
                    .findFirst()
                    .map(EventRecord::getCapacity)
                    .ifPresent(capacityList::add);

        }

        Set<Equipment> equipment = this.getEquipment();
        if (equipment != null) {
            equipment.stream()
                    .map(Equipment::getCapacity)
                    .forEach(capacityList::add);
        }

        return capacityList.stream()
                .reduce(this.capacity, (aLong, aLong2) -> {
                    if (aLong != 0 && aLong2 != 0) {
                        return (aLong < aLong2) ? aLong : aLong2;
                    } else {
                        return aLong2 == 0 ? aLong : aLong2;
                    }
                });
    }

    @JsonIgnore
    public Float getSalePrice() {
        return this.event != null ? this.event.getSalePrice() : 0;
    }

    @JsonIgnore
    public Float getPurchasePrice() {
        return this.event != null
                ? this.event.getPurchasePrice()
                : 0;
    }

    @JsonIgnore
    public String getEventName() {
        return this.event != null ? this.event.getName() : "Archived";
    }

    @JsonIgnore
    public String getDescription() {
        return name;
    }
}