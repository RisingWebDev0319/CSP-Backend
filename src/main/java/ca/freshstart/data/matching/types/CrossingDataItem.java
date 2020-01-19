package ca.freshstart.data.matching.types;

import ca.freshstart.data.client.entity.Client;
import ca.freshstart.data.room.entity.Room;
import ca.freshstart.data.therapist.entity.Therapist;
import ca.freshstart.types.Constants;
import ca.freshstart.applications.client.helpers.CspTimeDeserializer;
import ca.freshstart.applications.client.helpers.CspTimeSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.ToString;

import java.sql.Time;
import java.util.List;

@Data
@ToString
public class CrossingDataItem {

    @JsonFormat(pattern = Constants.TIME_FORMAT)
    @JsonSerialize(using = CspTimeSerializer.class)
    @JsonDeserialize(using = CspTimeDeserializer.class)
    private Time time;

    private Integer duration;

    private Therapist therapist;

    private Room room;

    private List<Client> clients;

    private CrossingDataType type;

    public CrossingDataItem(Time time, Integer duration, Therapist therapist, Room room, List<Client> clients, CrossingDataType type) {
        this.time = time;
        this.duration = duration;
        this.therapist = therapist;
        this.room = room;
        this.clients = clients;
        this.type = type;
    }
}
