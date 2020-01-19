package ca.freshstart.data.client.entity;

import ca.freshstart.types.Constants;
import ca.freshstart.types.ExternalIdIf;
import ca.freshstart.types.Updateable;
import ca.freshstart.types.EntityId;
import ca.freshstart.helpers.remote.types.ClientInformationRemote;
import ca.freshstart.helpers.remote.types.ClientRemote;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

import java.util.Date;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class Client extends EntityId implements ExternalIdIf<Long>, Updateable<ClientRemote> {

    //    @JsonIgnore
    private Long externalId;

    private String name;

    private String recordType;

    private String entityId;

    @Column(columnDefinition = "TEXT")
    private String goals;

    @Column(columnDefinition = "TEXT")
    private String reasons;

    @JsonIgnore
    private boolean archived = false;

    @Column(length = 80, name = "first_name")
    private String firstName;

    @Column(length = 80, name = "last_name")
    private String lastName;

    @Column(length = 80, name = "preferred_name")
    private String preferredName;

    @Column(length = 320, name = "email", unique = true)
    private String email;

    @Column(length = 50, name = "phone_mobile")
    private String phoneMobile;

    @Column(length = 50, name = "phone_home")
    private String phoneHome;

    @Column(length = 50, name = "phone_work")
    private String phoneWork;

    @Column(name = "client_since")
    @JsonFormat(pattern = Constants.DATE_FORMAT)
    @Temporal(TemporalType.DATE)
    private Date clientSince;

    @Column(name = "birthday")
    @JsonFormat(pattern = Constants.DATE_FORMAT)
    @Temporal(TemporalType.DATE)
    private Date birthday;

    @Column(length = 80, name = "street")
    private String street;

    @Column(length = 20, name = "city")
    private String city;

    @Column(length = 50, name = "postcode")
    private String postcode;

    @Column(length = 50, name = "state")
    private String state;

    @Column(length = 80, name = "country")
    private String country;

    @Column(name = "gender")
    private int gender;

    @Column(name = "program_length")
    private String programLength;

    @Column(name = "cycle")
    private String cycle;

    public Client() {
    }

    public Client(ClientRemote remote, ClientInformationRemote informationRemote) {
        externalId = remote.getId();
        update(remote);
        updateFromRemoteInfo(informationRemote);
    }

    public Client(ClientRemote remote) {
        externalId = remote.getId();
        update(remote);
    }

    public void updateFromRemoteInfo(ClientInformationRemote informationRemote) {
        if (informationRemote == null) {
            return;
        }

        setGoals(informationRemote.getGoals());
        setReasons(informationRemote.getReasons());
    }

    public void update(ClientRemote remote) {
        if (remote == null) {
            return;
        }

        ClientRemote.ClientColumnsRemote columns = remote.getColumns();
        setName(columns.getAltname());
        setRecordType(remote.getRecordtype());
        setEntityId(columns.getEntityid());
        setFirstName(" ");
        setEmail(" ");
    }

    @Transient
    public String getReadbleName() {
        return (firstName != null ? firstName : "") +
                ((firstName != null && lastName != null) ? " " : "") +
                (lastName != null ? lastName : "");
    }

}
