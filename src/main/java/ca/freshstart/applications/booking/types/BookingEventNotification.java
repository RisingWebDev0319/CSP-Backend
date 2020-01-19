package ca.freshstart.applications.booking.types;

import ca.freshstart.data.concreteEvent.entity.ConcreteEvent;
import ca.freshstart.types.CrudAction;
import ca.freshstart.types.Constants;
import ca.freshstart.helpers.CspDateSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.util.Date;

@Data
public class BookingEventNotification {

    private Long concreteEventId;

    @JsonFormat(pattern = Constants.DATE_FORMAT)
    @JsonSerialize(using = CspDateSerializer.class)
    private Date concreteEventDate;

    private CrudAction action;

    public BookingEventNotification(ConcreteEvent event, CrudAction action) {
        this.concreteEventId = event.getId();
        this.concreteEventDate = event.getDate();
        this.action = action;
    }

}
