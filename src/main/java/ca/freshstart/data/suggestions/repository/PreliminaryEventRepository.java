package ca.freshstart.data.suggestions.repository;

import ca.freshstart.data.suggestions.entity.PreliminaryEvent;
import ca.freshstart.data.matching.types.PreliminaryEventType;
import ca.freshstart.types.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PreliminaryEventRepository extends BaseRepository<PreliminaryEvent, Long> {

    @Query("SELECT dataItem FROM PreliminaryEvent dataItem " +
            "WHERE dataItem.fk.dataFK.id.session.id = :sessionId " +
            "AND dataItem.fk.dataFK.id.client.id = :clientId")
    List<PreliminaryEvent> findBySessionIdAndClientId(@Param("sessionId") Long sessionId,
                                                      @Param("clientId") Long clientId);

    @Query("SELECT dataItem FROM PreliminaryEvent dataItem" +
            " WHERE dataItem.fk.dataFK.id.session.id = :sessionId" +
            " AND dataItem.fk.dataFK.id.client.id = :clientId" +
            " AND dataItem.state = :state" +
            " AND dataItem.fk.date = :date")
    List<PreliminaryEvent> findBySessionIdAndClientIdAndStateAndDate(@Param("sessionId") Long sessionId,
                                                                     @Param("clientId") Long clientId,
                                                                     @Param("state") PreliminaryEventType state,
                                                                     @Param("date") Date date);

    @Query("SELECT dataItem FROM PreliminaryEvent dataItem" +
            " WHERE dataItem.state = :state" +
            " AND dataItem.fk.date IN (:dates)")
    List<PreliminaryEvent> findByStateInDates(@Param("state") PreliminaryEventType state,
                                              @Param("dates") Set<Date> dates);

    @Query("SELECT dataItem FROM PreliminaryEvent dataItem " +
            "WHERE dataItem.fk.dataFK.id.session.id = :sessionId " +
            "AND dataItem.fk.dataFK.id.client.id = :clientId " +
            "AND dataItem.state IS NULL")
    List<PreliminaryEvent> findBySessionIdAndClientIdAndNullState(@Param("sessionId") Long sessionId,
                                                                  @Param("clientId") Long clientId);

    @Query("SELECT dataItem FROM PreliminaryEvent dataItem " +
            "WHERE dataItem.fk.dataFK.id.session.id = :sessionId " +
            "AND dataItem.state = :state")
    List<PreliminaryEvent> findBySessionIdAndState(@Param("sessionId") Long sessionId,
                                                   @Param("state") PreliminaryEventType state);

    @Query("SELECT dataItem FROM PreliminaryEvent dataItem " +
            "WHERE dataItem.fk.dataFK.id.session.id = :sessionId " +
            "AND dataItem.state IS NULL")
    List<PreliminaryEvent> findBySessionIdAndNullState(@Param("sessionId") Long sessionId);

    @Query("SELECT dataItem FROM PreliminaryEvent dataItem " +
            "WHERE dataItem.fk.dataFK.id.session.id = :sessionId " +
            "AND dataItem.fk.dataFK.id.client.id = :clientId " +
            "AND dataItem.fk.service.id = :serviceId ")
    List <PreliminaryEvent> findBySessionIdAndClientIdAndServiceId(@Param("sessionId") Long sessionId,
                                                                      @Param("clientId") Long clientId,
                                                                      @Param("serviceId") Long serviceId);

    @Query("SELECT dataItem FROM PreliminaryEvent dataItem " +
            "WHERE dataItem.fk.dataFK.id.session.id = :sessionId " +
            "AND dataItem.fk.dataFK.id.client.id = :clientId " +
            "AND dataItem.fk.service.id = :serviceId " +
            "AND dataItem.fk.date = :dateValue "
    )
    Optional<PreliminaryEvent> findBySessionIdAndClientIdAndServiceIdAndDate(@Param("sessionId") Long sessionId,
                                                                             @Param("clientId") Long clientId,
                                                                             @Param("serviceId") Long serviceId,
                                                                             @Param("dateValue") Date dateValue
    );

    @Transactional
    @Modifying
    @Query("DELETE FROM PreliminaryEvent  dataItem WHERE dataItem.id = :Id")
    void deleteById(@Param("Id") Long Id);
}
