package ca.freshstart.data.client.repository;

import ca.freshstart.data.client.entity.Client;
import ca.freshstart.types.BaseRepository;
import ca.freshstart.types.ExternalIdRepositoryIf;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClientRepository extends BaseRepository<Client, Long>, ExternalIdRepositoryIf<Client, Long> {
    @Query("SELECT c FROM Client c where archived = false")
    List<Client> findAll();

    @Query("SELECT c FROM Client c where archived = false")
    List<Client> findAllAsList(Pageable pageable);

    Page<Client> findAll(Specification<Client> spec, Pageable pageable);

    @Query("SELECT count(id) FROM Client where archived = false")
    long count();

    @Query("SELECT c FROM Client c WHERE externalId IN (:ids)")
    List<Client> findByExternalIds(@Param("ids") List<Long> ids);

    Client findClientByEmail(String email);

    boolean existsClientByEmail(String email);
}
