package ca.freshstart.data.concreteEvent.types;

import ca.freshstart.types.Constants;
import ca.freshstart.applications.client.helpers.CspTimeDeserializer;
import ca.freshstart.applications.client.helpers.CspTimeSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.sql.Time;

/**
 * Info about crosses with event
 */
@Data
public class CrossingItem {

    private CrossingType crossType;

    @JsonFormat(pattern = Constants.TIME_FORMAT)
    @JsonSerialize(using = CspTimeSerializer.class)
    @JsonDeserialize(using = CspTimeDeserializer.class)
    private Time time;

    private int duration;

    private String clientName;

    private String serviceName;

    private String roomName;
}
