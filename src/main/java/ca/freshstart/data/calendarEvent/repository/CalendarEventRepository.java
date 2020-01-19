package ca.freshstart.data.calendarEvent.repository;

import ca.freshstart.data.calendarEvent.entity.CalendarEvent;
import ca.freshstart.types.BaseRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CalendarEventRepository extends BaseRepository<CalendarEvent, Long> {

    @Query("SELECT t FROM CalendarEvent t where archived = false")
    List<CalendarEvent> findAll();

    @Query("SELECT t FROM CalendarEvent t where archived = false and t.name=:name")
    List<CalendarEvent> findBy(@Param("name") String name);

    @Query("SELECT t FROM CalendarEvent t where archived = false")
    List<CalendarEvent> findAllAsList(Pageable pageable);

    @Query("SELECT count(id) FROM CalendarEvent where archived = false")
    long count();

    @Query("SELECT t FROM CalendarEvent t where t.therapist is null and archived = false and (t.dateStart - CURRENT_DATE) < 7")
    List<CalendarEvent> findAllUrgent(Pageable pageable);

    @Query("SELECT count(id) FROM CalendarEvent where therapist is null and archived = false and (dateStart - CURRENT_DATE) < 7")
    long countUrgent();
}
