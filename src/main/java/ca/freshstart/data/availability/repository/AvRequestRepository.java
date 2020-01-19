package ca.freshstart.data.availability.repository;

import ca.freshstart.data.availability.entity.AvRequest;
import ca.freshstart.types.BaseRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AvRequestRepository extends BaseRepository<AvRequest, Long> {

    @Query("SELECT t FROM AvRequest t WHERE archived = false")
    List<AvRequest> findAll();

    @Query("SELECT t FROM AvRequest t WHERE archived = false")
    List<AvRequest> findAllAsList(Pageable pageable);

    @Query("SELECT count(id) FROM AvRequest WHERE archived = false")
    long count();

    @Query("SELECT t FROM AvRequest t WHERE t.archived = false" +
            " AND EXISTS (SELECT 1 FROM t.therapistsRequests z WHERE z.therapistId = :therapistId)")
    List<AvRequest> findRequestsByTherapistId(@Param("therapistId") Long therapistId, Pageable pageable);

    @Query("SELECT count(id) FROM AvRequest t where t.archived = false AND" +
            " EXISTS (SELECT 1 FROM t.therapistsRequests z WHERE z.therapistId = :therapistId)")
    long countRequestsByTherapistId(@Param("therapistId") Long therapistId);
}