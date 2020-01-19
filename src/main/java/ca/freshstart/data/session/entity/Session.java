package ca.freshstart.data.session.entity;

import ca.freshstart.data.client.entity.Client;
import ca.freshstart.types.EntityId;
import ca.freshstart.data.therapist.entity.Therapist;
import ca.freshstart.types.Constants;
import ca.freshstart.helpers.CspDateSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Data
@Table(
        uniqueConstraints=
        @UniqueConstraint(name = "session_name_dates_uk", columnNames={"name", "startDate", "endDate"})
)
@EqualsAndHashCode(callSuper = true)
public class Session extends EntityId {

    @Column(nullable = false, length = 255)
    private String name;

    @JsonFormat(pattern = Constants.DATE_FORMAT)
    @JsonSerialize(using = CspDateSerializer.class)
    @Temporal(TemporalType.DATE)
    private Date startDate;

    @JsonFormat(pattern = Constants.DATE_FORMAT)
    @JsonSerialize(using = CspDateSerializer.class)
    @Temporal(TemporalType.DATE)
    private Date endDate;

    @JsonIgnore
    private boolean archived = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @JoinTable(name="session_clients",
            joinColumns={@JoinColumn(name="session_id")},
            inverseJoinColumns={@JoinColumn(name="client_id")})
    private Set<Client> clients;

    @ManyToMany(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @JoinTable(name="session_therapists",
            joinColumns={@JoinColumn(name="session_id")},
            inverseJoinColumns={@JoinColumn(name="therapist_id")})
    private Set<Therapist> therapists;
}