package ca.freshstart.data.concreteCalendarEvent.repository;

import ca.freshstart.data.concreteCalendarEvent.entity.ConcreteCalendarEvent;
import ca.freshstart.types.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Time;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConcreteCalendarEventRepository extends BaseRepository<ConcreteCalendarEvent, Long> {

    @Query("SELECT e FROM ConcreteCalendarEvent e" +
            " WHERE e.calendarEvent.id = :calendarEventId" +
            " AND e.archived = false" +
            " AND e.date >= :startDate" +
            " AND e.date <= :endDate")
    List<ConcreteCalendarEvent> findInRange(@Param("calendarEventId") Long calendarEventId,
                                            @Param("startDate") Date startDate,
                                            @Param("endDate") Date endDate);

    @Query("SELECT e FROM ConcreteCalendarEvent e" +
            " WHERE e.archived = false" +
            " AND e.date >= :startDate" +
            " AND e.date <= :endDate")
    List<ConcreteCalendarEvent> findAllInRange(@Param("startDate") Date startDate,
                                               @Param("endDate") Date endDate);

    @Query("SELECT e FROM ConcreteCalendarEvent e" +
            " WHERE e.archived = false" +
            " AND e.therapist.id = :therapistId" +
            " AND e.date >= :startDate" +
            " AND e.date <= :endDate")
    List<ConcreteCalendarEvent> findByTherapistAndDateInRange(@Param("therapistId") Long therapistId,
                                                              @Param("startDate") Date startDate,
                                                              @Param("endDate") Date endDate);

    @Query("SELECT e FROM ConcreteCalendarEvent e" +
            " WHERE e.calendarEvent.id = :calendarEventId" +
            " AND e.id = :id" +
            " AND e.archived = false")
    Optional<ConcreteCalendarEvent> findByConcreteId(@Param("calendarEventId") Long calendarEventId,
                                                     @Param("id") Long id);

    @Query("SELECT e FROM ConcreteCalendarEvent e" +
            " WHERE e.calendarEvent.id = :calendarEventId" +
            " AND e.archived = false")
    List<ConcreteCalendarEvent> findByCalendarEventId(@Param("calendarEventId") Long calendarEventId);

    @Query("SELECT e FROM ConcreteCalendarEvent e" +
            " WHERE e.calendarEvent.id = :calendarEventId" +
            " AND e.archived = false" +
            " AND (e.date > :sinceDate OR (e.date = :sinceDate AND e.time > :sinceTime))")
    List<ConcreteCalendarEvent> findByCalendarEventIdAfterDate(@Param("calendarEventId") Long calendarEventId,
                                                               @Param("sinceDate") Date sinceDate,
                                                               @Param("sinceTime") Time sinceTime);

}
