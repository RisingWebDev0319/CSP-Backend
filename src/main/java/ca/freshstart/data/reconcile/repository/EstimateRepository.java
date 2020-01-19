package ca.freshstart.data.reconcile.repository;


import ca.freshstart.data.reconcile.entity.Estimate;
import ca.freshstart.types.BaseRepository;
import ca.freshstart.types.ExternalIdRepositoryIf;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EstimateRepository extends BaseRepository<Estimate, Long>, ExternalIdRepositoryIf<Estimate, Long> {

    @Query("SELECT e FROM Estimate e WHERE e.sent = false")
    List<Estimate> findUnsent(Pageable pageable);

    @Query("SELECT count(id) FROM Estimate WHERE sent = false")
    long findUnsentCount();

    @Query("SELECT count(id) FROM Estimate")
    long countAll();

    @Query("SELECT e FROM Estimate e WHERE e.sent = false AND e.clientId = :clientId")
    List<Estimate> findUnsentByClientId(@Param("clientId") Long clientId);

    @Query("SELECT e FROM Estimate e WHERE e.sent = false AND e.id = :id")
    Optional<Estimate> findUnsentById(@Param("id") Long id);

    @Query("SELECT e FROM Estimate e WHERE e.externalId IN (:ids)")
    List<Estimate> findByExternalIds(@Param("ids") List<Long> ids);

}