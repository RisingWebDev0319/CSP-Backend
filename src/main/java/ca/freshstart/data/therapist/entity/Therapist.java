package ca.freshstart.data.therapist.entity;

import ca.freshstart.data.eventRecord.entity.EventRecord;
import ca.freshstart.data.room.entity.Room;
import ca.freshstart.data.service.entity.Service;
import ca.freshstart.data.week.entity.Week;
import ca.freshstart.types.ExternalIdIf;
import ca.freshstart.types.Updateable;
import ca.freshstart.types.EntityId;
import ca.freshstart.data.serviceCategory.entity.ServiceCategory;
import ca.freshstart.helpers.remote.types.TherapistRemote;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.*;
import java.util.Set;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Therapist extends EntityId implements ExternalIdIf<String>, Updateable<TherapistRemote> {

    @Column(unique = true)
//    @JsonIgnore
    private String externalId;

    private String name;

    @Column(unique = true)
    private String email;

    @JsonIgnore
    private boolean archived = false;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "therapist_info_id")
    private TherapistInfo therapistInfo;



    @ManyToMany(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @JoinTable(
            name = "therapist_service_categories",
            joinColumns = {@JoinColumn(name = "therapist_id")},
            inverseJoinColumns = {@JoinColumn(name = "service_category_id")}
    )
    private Set<ServiceCategory> serviceCategories;

    @ManyToMany(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @JoinTable(name = "therapist_services",
            joinColumns = {@JoinColumn(name = "therapist_id")},
            inverseJoinColumns = {@JoinColumn(name = "service_id")})
    private Set<Service> services;

    @ManyToMany(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @JoinTable(name = "therapist_events",
            joinColumns = {@JoinColumn(name = "therapist_id")},
            inverseJoinColumns = {@JoinColumn(name = "event_id")})
    private Set<EventRecord> events;

    @ManyToMany(mappedBy = "therapists", fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @JsonIgnore
    private Set<Week> week;

    @ManyToMany(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @JoinTable(name = "therapist_preferred_rooms",
            joinColumns = {@JoinColumn(name = "therapist_id")},
            inverseJoinColumns = {@JoinColumn(name = "room_id")})
    private Set<Room> preferredRooms;




    public Therapist() {
    }

    public Therapist(TherapistRemote remote) {
        this.externalId = remote.getId();
        this.email = remote.getEmail();
        update(remote);
    }

    public TherapistInfo getTherapistInfo(){
        return this.therapistInfo;
    }

    public void update(TherapistRemote remote) {
        if (remote == null) {
            return;
        }

        setName(remote.getName());
    }
}
