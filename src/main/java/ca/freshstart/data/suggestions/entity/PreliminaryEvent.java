package ca.freshstart.data.suggestions.entity;

import ca.freshstart.data.client.entity.Client;
import ca.freshstart.data.concreteEvent.interfaces.BaseConcreteEvent;
import ca.freshstart.data.room.entity.Room;
import ca.freshstart.data.service.entity.Service;
import ca.freshstart.data.session.entity.Session;
import ca.freshstart.data.suggestions.types.PreliminaryEventFK;
import ca.freshstart.data.therapist.entity.Therapist;
import ca.freshstart.data.matching.entity.ClientMatchingConfirmation;
import ca.freshstart.types.Duration;
import ca.freshstart.data.matching.types.PreliminaryEventType;
import ca.freshstart.types.EntityId;
import ca.freshstart.types.Constants;
import ca.freshstart.helpers.CspDateSerializer;
import ca.freshstart.applications.client.helpers.CspTimeDeserializer;
import ca.freshstart.applications.client.helpers.CspTimeSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class PreliminaryEvent extends EntityId implements BaseConcreteEvent {

    @AttributeOverrides({
            @AttributeOverride(name = "service",
                    column = @Column(name = "service_id")),
            @AttributeOverride(name = "date",
                    column = @Column(name = "date")),
            @AttributeOverride(name = "client",
                    column = @Column(name = "client_id")),
            @AttributeOverride(name = "session",
                    column = @Column(name = "session_id"))
    })
    @Embedded
    PreliminaryEventFK fk;

    private String note;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "therapist_id")
    private Therapist therapist;

    private Duration duration;

    @JsonFormat(pattern = Constants.TIME_FORMAT)
    @JsonSerialize(using = CspTimeSerializer.class)
    @JsonDeserialize(using = CspTimeDeserializer.class)
    private Time time;

    @Enumerated(EnumType.STRING)
    private PreliminaryEventType state;

    @ManyToOne
    @JsonIgnore
    private ClientMatchingConfirmation matchingConfirmation;


    public PreliminaryEventFK getFk() {
        if (fk == null) fk = new PreliminaryEventFK();
        return fk;
    }

    @Transient
    public Long getServiceId() {
        return getFk().getService().getId();
    }

    public SsSessionClientData getDataFK() {
        if (getFk().getDataFK() == null) setDataFK(new SsSessionClientData());
        return getFk().getDataFK();
    }

    @Transient
    @JsonIgnore
    public void setDataFK(SsSessionClientData dataFK) {
        getFk().setDataFK(dataFK);
    }

    @Transient
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATE_FORMAT)
    public Date getDate() {
        return getFk().getDate();
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATE_FORMAT)
    @JsonSerialize(using = CspDateSerializer.class)
    @Temporal(TemporalType.DATE)
    public void setDate(Date inputDate) {
        getFk().setDate(inputDate);
    }

    public Long getTherapistId() {
        return therapist != null ? therapist.getId() : null;
    }

    @Override
    public List<Long> getClientsIds() {
        List<Long> clientsIds = new ArrayList<>();
        clientsIds.add(this.getFk().getClient().getId());
        return clientsIds;
    }

    @Transient
    public Session getSession(){
        return this.getFk().getSession();
    }

    @Transient
    public Service getService(){
        return this.getFk().getService();
    }

    @Transient
    public void setService(Service service){
        this.getFk().setService(service);
    }

    @Transient
    public Client getClient(){
        return this.getFk().getClient();
    }

    @Transient
    public Long getClientId(){
        return getClient().getId();
    }
}
