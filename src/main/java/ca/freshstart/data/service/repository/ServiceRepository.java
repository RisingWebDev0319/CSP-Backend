package ca.freshstart.data.service.repository;

import ca.freshstart.data.service.entity.Service;
import ca.freshstart.data.taxes.entity.Taxes;
import ca.freshstart.types.BaseRepository;
import ca.freshstart.types.ExternalIdRepositoryIf;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServiceRepository extends BaseRepository<Service, Long>, ExternalIdRepositoryIf<Service, Long> {

    @Query("SELECT t FROM Service t where archived = false")
    List<Service> findAllAsList(Pageable pageable);

    @Query("SELECT s FROM Service s WHERE s.categories IS EMPTY")
    List<Service> findAllUncotegorized(Pageable pageable);

    @Query("SELECT t FROM Service t WHERE externalId IN (:ids)")
    List<Service> findByExternalIds(@Param("ids") List<Long> ids);

    @Query("SELECT count(id) FROM Service s WHERE s.categories IS EMPTY")
    long countUncategorized();

    @Query("SELECT count(id) FROM Service where archived = false")
    long count();

    @Query("SELECT c FROM Service c where archived = false AND c.tax = :taxId")
    List<Service> findByTaxId(@Param("taxId") Long taxId);
}
