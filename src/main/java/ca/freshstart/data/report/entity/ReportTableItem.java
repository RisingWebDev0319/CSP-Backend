package ca.freshstart.data.report.entity;

import ca.freshstart.data.client.entity.Client;
import ca.freshstart.data.room.entity.Room;
import ca.freshstart.data.therapist.entity.Therapist;
import ca.freshstart.types.Constants;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import java.sql.Time;
import java.util.Date;
import java.util.List;

@Data
public class ReportTableItem {

    @JsonFormat(pattern = Constants.DATE_FORMAT)
    @Temporal(TemporalType.DATE)
    private Date date;

    private String serviceName;

    private Float price;

    private Time time;

    private Integer duration;

    private Room room;

    private Therapist therapist;

    private Client client;
}
