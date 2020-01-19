package ca.freshstart.data.concreteEvent.repositories;

import ca.freshstart.data.concreteEvent.entity.ConcreteEvent;
import ca.freshstart.data.service.entity.Service;
import ca.freshstart.types.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface ConcreteEventRepository extends BaseRepository<ConcreteEvent, Long> {

    @Query("SELECT e FROM ConcreteEvent e WHERE e.date = :date")
    List<ConcreteEvent> findByDate(@Param("date") Date date);


    @Query("SELECT e FROM ConcreteEvent e WHERE e.date IN (:dates)")
    List<ConcreteEvent> findInDates(@Param("dates") Set<Date> dates);

    @Query("SELECT e FROM ConcreteEvent e" +
            " WHERE e.date >= :dateStart" +
            " AND e.date <= :dateEnd")
    List<ConcreteEvent> findByDateInRange(@Param("dateStart") Date dateStart,
                                          @Param("dateEnd") Date dateEnd);

    @Query("SELECT e FROM ConcreteEvent e" +
            " WHERE e.therapist.id = :therapistId" +
            " AND e.date >= :dateStart" +
            " AND e.date <= :dateEnd")
    List<ConcreteEvent> findByTherapistAndDateInRange(@Param("therapistId") Long therapistId,
                                                      @Param("dateStart") Date dateStart,
                                                      @Param("dateEnd") Date dateEnd);

}
