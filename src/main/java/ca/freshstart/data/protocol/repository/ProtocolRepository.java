package ca.freshstart.data.protocol.repository;

import ca.freshstart.data.protocol.entity.Protocol;
import ca.freshstart.types.BaseRepository;
import ca.freshstart.types.ExternalIdRepositoryIf;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProtocolRepository extends BaseRepository<Protocol, Long>, ExternalIdRepositoryIf<Protocol, Long> {
    @Query("SELECT p FROM Protocol p WHERE p.externalId IN (:ids)")
    List<Protocol> findByExternalIds(@Param("ids") List<Long> ids);

    @Query("SELECT p FROM Protocol p WHERE p.packageSign = true")
    List<Protocol> findProtocolsPackages();

    @Query("SELECT p FROM Protocol p WHERE p.packageSign = false")
    List<Protocol> findProtocols();
}