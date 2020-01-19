package ca.freshstart.data.availability.entity;

import ca.freshstart.types.EntityId;
import ca.freshstart.types.Constants;
import ca.freshstart.helpers.CspDateSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class AvHistoryState extends EntityId {

    public AvHistoryState() {
    }

    public AvHistoryState(Date date, AvTimeRecord timeRecord) {
        setDate(date);
        setTimeStart(timeRecord.getTimeStart());
        setTimeEnd(timeRecord.getTimeEnd());
        setType(timeRecord.getType());
    }

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern = Constants.DATE_FORMAT)
    @JsonSerialize(using = CspDateSerializer.class)
    private Date date;

    @Column(nullable = false)
    private Long timeStart;// minutes from day start, less than 24*60

    @Column(nullable = false)
    private Long timeEnd;// minutes from day start, less or equal 24*60, more than timeStart

    private String type;// A - for available, U - for unavailable. no record for undefined

}
