package ca.freshstart.data.concreteEvent.interfaces;

import ca.freshstart.types.Duration;
import ca.freshstart.data.room.entity.Room;
import ca.freshstart.data.therapist.entity.Therapist;

import java.sql.Time;
import java.util.Date;
import java.util.List;

public interface BaseCrossEvent {

    Room getRoom();

    Therapist getTherapist();

    Date getDate();

    Time getTime();

    Duration getDuration();

    List<Long> getClientsIds();
}
