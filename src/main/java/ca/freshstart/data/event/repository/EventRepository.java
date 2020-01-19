package ca.freshstart.data.event.repository;

import ca.freshstart.data.event.entity.Event;
import ca.freshstart.types.BaseRepository;
import ca.freshstart.types.ExternalIdRepositoryIf;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventRepository extends BaseRepository<Event, Long>, ExternalIdRepositoryIf<Event, Long> {
    @Query("SELECT t FROM Event t WHERE externalId IN (:ids)")
    List<Event> findByExternalIds(@Param("ids") List<Long> ids);

    @Query("SELECT r FROM Event r WHERE archived = false AND r.eventType.id = :typeId")
    List<Event> findByTypeId(@Param("typeId")  Long typeId);

    @Query("SELECT r FROM Event r WHERE archived = false AND r.tax.id = :taxId")
    List<Event> findByTaxId(@Param("taxId")  Long taxId);

    @Query("SELECT t FROM Event t where archived = false")
    List<Event> findAllAsList(Pageable pageable);

    @Query("SELECT count(id) FROM Event where archived = false")
    long count();
}
