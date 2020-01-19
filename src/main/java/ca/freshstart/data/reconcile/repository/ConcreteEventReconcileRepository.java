package ca.freshstart.data.reconcile.repository;


import ca.freshstart.data.reconcile.entity.ConcreteEventReconcile;
import ca.freshstart.types.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface ConcreteEventReconcileRepository extends BaseRepository<ConcreteEventReconcile, Long> {
    @Query("SELECT e FROM ConcreteEventReconcile e" +
            " WHERE e.concreteEvent.date = :date" +
            " ORDER BY e.id ASC")
    List<ConcreteEventReconcile> findByDate(@Param("date") Date date);

    @Query("SELECT e FROM ConcreteEventReconcile e" +
            " WHERE e.concreteEvent.date = :date" +
            " AND e.concreteEvent.client.id = :clientId" +
            " ORDER BY e.id ASC")
    List<ConcreteEventReconcile> findByDateAndClientId(@Param("date") Date date,
                                                       @Param("clientId") Long clientId);


    @Query("SELECT e FROM ConcreteEventReconcile e" +
            " WHERE e.concreteEvent.date IN (:dates)" +
            " ORDER BY e.id ASC")
    List<ConcreteEventReconcile> findInDates(@Param("dates") Set<Date> dates);


    @Query("SELECT e FROM ConcreteEventReconcile e" +
            " WHERE e.concreteEvent.date >= :dateStart" +
            " AND e.concreteEvent.date <= :dateEnd" +
            " ORDER BY e.id ASC")
    List<ConcreteEventReconcile> findByDateInRange(@Param("dateStart") Date dateStart,
                                                   @Param("dateEnd") Date dateEnd);

    @Query("SELECT e FROM ConcreteEventReconcile e" +
            " WHERE e.concreteEvent.date >= :dateStart" +
            " AND e.concreteEvent.date <= :dateEnd" +
            " AND e.concreteEvent.client.id = :clientId" +
            " ORDER BY e.id ASC")
    List<ConcreteEventReconcile> findByDateInRangeAndClientId(@Param("dateStart") Date dateStart,
                                                              @Param("dateEnd") Date dateEnd,
                                                              @Param("clientId") Long clientId);


    @Query("SELECT e FROM ConcreteEventReconcile e" +
            " WHERE e.concreteEvent.therapist.id = :therapistId" +
            " AND e.concreteEvent.date >= :dateStart" +
            " AND e.concreteEvent.date <= :dateEnd" +
            " ORDER BY e.id ASC")
    List<ConcreteEventReconcile> findByTherapistAndDateInRange(@Param("therapistId") Long therapistId,
                                                               @Param("dateStart") Date dateStart,
                                                               @Param("dateEnd") Date dateEnd);

    @Query("SELECT e FROM ConcreteEventReconcile e" +
            " WHERE e.id IN (:ids)" +
            " ORDER BY e.id ASC")
    Set<ConcreteEventReconcile> findInIds(@Param("ids") List<Long> ids);

}
