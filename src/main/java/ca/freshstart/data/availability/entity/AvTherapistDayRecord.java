package ca.freshstart.data.availability.entity;

import ca.freshstart.types.EntityId;
import ca.freshstart.data.therapist.entity.Therapist;
import ca.freshstart.types.Constants;
import ca.freshstart.helpers.CspDateSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
@ToString(exclude = {"therapist"})
@EqualsAndHashCode(exclude = {"therapist"}, callSuper = true)
@Table(uniqueConstraints={
        @UniqueConstraint(columnNames = {"date", "therapist_id"})
})
public class AvTherapistDayRecord extends EntityId {

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern = Constants.DATE_FORMAT)
    @JsonSerialize(using = CspDateSerializer.class)
    private Date date;

    @ManyToOne
    @JoinColumn(name = "therapist_id", nullable = false)
    @JsonIgnore
    private Therapist therapist;

    @OneToMany(mappedBy = "therapistDayRecord", fetch = FetchType.EAGER,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @Column(nullable = false)
    private List<AvTimeRecord> timeItems = new ArrayList<>();

    public void setTimeItems(List<AvTimeRecord> timeItems) {
        this.timeItems.clear();

        if(timeItems != null) {
            timeItems.forEach(time -> time.setTherapistDayRecord(this));
            this.timeItems.addAll(timeItems);
        }
    }
}