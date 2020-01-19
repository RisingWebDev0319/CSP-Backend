package ca.freshstart.data.concreteEvent.repositories;


import ca.freshstart.data.concreteEvent.entity.ConcreteEventChange;
import ca.freshstart.types.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ConcreteEventChangeCodeRepository extends BaseRepository<ConcreteEventChange, Long> {

    @Query("SELECT ecc FROM ConcreteEventChange ecc WHERE ecc.eventCode = :eventCode")
    Optional<ConcreteEventChange> findByEventCode(@Param("eventCode") String eventCode);

    @Modifying
    @Transactional
    @Query("DELETE from ConcreteEventChange ecc WHERE ecc.concreteEventId = :concreteEventId")
    void deleteByConcreteEventId(@Param("concreteEventId") Long concreteEventId);

}