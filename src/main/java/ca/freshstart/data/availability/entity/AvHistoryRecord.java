package ca.freshstart.data.availability.entity;

import ca.freshstart.data.appUser.entity.AppUser;
import ca.freshstart.types.EntityId;
import ca.freshstart.data.availability.types.UserSimple;
import ca.freshstart.types.Constants;
import ca.freshstart.helpers.CspDateSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class AvHistoryRecord extends EntityId {

    public AvHistoryRecord(Long therapistId, Date editDate, AvHistoryState prevState, AvHistoryState newState, AppUser user) {
        this.therapistId = therapistId;
        this.editDate = editDate;
        this.prevState = prevState;
        this.newState = newState;
        this.user = user;
    }

    @JsonIgnore
    private Long therapistId;

    @JsonFormat(pattern = Constants.DATE_FORMAT)
    @JsonSerialize(using = CspDateSerializer.class)
    @Temporal(TemporalType.DATE)
    private Date editDate;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "prev_state_id", unique = true, updatable = false)
    private AvHistoryState prevState;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "new_state_id", unique = true, updatable = false)
    private AvHistoryState newState;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private AppUser user;

    @JsonGetter("userSimple")
    public UserSimple getUserSimple() {
        return UserSimple.fromUser(user);
    }

}