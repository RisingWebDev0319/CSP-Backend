package ca.freshstart.data.report.entity;

import ca.freshstart.data.client.entity.Client;
import ca.freshstart.data.report.enums.ReportType;
import ca.freshstart.data.room.entity.Room;
import ca.freshstart.data.session.entity.Session;
import ca.freshstart.data.therapist.entity.Therapist;
import ca.freshstart.types.Constants;
import ca.freshstart.types.EntityId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;

import java.util.Date;
import java.util.Set;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class Report extends EntityId {

    @Enumerated(EnumType.STRING)
    private ReportType type;

    @Column(name = "date_from")
    @JsonFormat(pattern = Constants.DATE_FORMAT)
    @Temporal(TemporalType.DATE)
    private Date dateFrom;

    @Column(name = "date_to")
    @JsonFormat(pattern = Constants.DATE_FORMAT)
    @Temporal(TemporalType.DATE)
    private Date dateTo;

    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name = "session_id")
    private Session session;

    private boolean autoupdate;

    @Column(name = "selected_all")
    private boolean selectedAll;

    private String url;

    @JsonIgnore
    private boolean archived = false;

    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name = "therapist_id")
    private Therapist therapist;

    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id")
    private Room room;
}
