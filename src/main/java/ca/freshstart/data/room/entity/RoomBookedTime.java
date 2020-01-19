package ca.freshstart.data.room.entity;

import ca.freshstart.types.EntityId;
import lombok.*;

import javax.persistence.*;

@Entity
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class RoomBookedTime extends EntityId {

    //DateFormat
    private String date;
    //Room.getId()
    private Long roomId;
    //minutes
    private Long booked;
}
