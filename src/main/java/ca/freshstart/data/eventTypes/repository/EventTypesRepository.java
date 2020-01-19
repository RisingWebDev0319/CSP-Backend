package ca.freshstart.data.eventTypes.repository;

import ca.freshstart.data.eventTypes.entity.EventTypes;
import ca.freshstart.types.BaseRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventTypesRepository extends BaseRepository<EventTypes, Long> {

    @Query("SELECT t FROM EventTypes t where archived = false")
    List<EventTypes> findAll();

    @Query("SELECT t FROM EventTypes t where archived = false")
    List<EventTypes> findAllAsList(Pageable pageable);

    @Query("SELECT count(*) FROM EventTypes where archived = false")
    long count();
}
