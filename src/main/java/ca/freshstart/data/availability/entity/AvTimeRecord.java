package ca.freshstart.data.availability.entity;

import ca.freshstart.types.EntityId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Data
@ToString(exclude = "therapistDayRecord")
@EqualsAndHashCode(exclude = "therapistDayRecord", callSuper = true)
public class AvTimeRecord extends EntityId {

    private Long timeStart;// minutes from day start, less than 24*60
    private Long timeEnd;// minutes from day start, less or equal 24*60, more than timeStart

    private String type;// A - for available, U - for unavailable. no record for undefined

    @ManyToOne
    @JoinColumn(name = "av_therapist_day_record_id")
    @JsonIgnore
    private AvTherapistDayRecord therapistDayRecord;
}