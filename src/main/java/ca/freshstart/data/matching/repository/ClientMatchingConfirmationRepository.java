package ca.freshstart.data.matching.repository;

import ca.freshstart.data.matching.entity.ClientMatchingConfirmation;
import ca.freshstart.types.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientMatchingConfirmationRepository extends BaseRepository<ClientMatchingConfirmation, Long> {

    @Query("SELECT entity FROM ClientMatchingConfirmation entity WHERE entity.id.session.id = :sessionId")
    List<ClientMatchingConfirmation> findBySessionId(@Param("sessionId") Long sessionId);

    @Query("SELECT entity FROM ClientMatchingConfirmation entity" +
            " WHERE entity.id.session.id = :sessionId" +
            " AND entity.id.client.id = :clientId")
    Optional<ClientMatchingConfirmation> findBySessionIdAndClientId(@Param("sessionId") Long sessionId,
                                                                    @Param("clientId") Long clientId);

    @Query("SELECT entity FROM ClientMatchingConfirmation entity WHERE entity.secret = :secret")
    Optional<ClientMatchingConfirmation> findBySecret(@Param("secret") String secret);
}
