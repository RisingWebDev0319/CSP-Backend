package ca.freshstart.data.session.repository;

import ca.freshstart.data.client.entity.Client;
import ca.freshstart.data.session.entity.Session;
import ca.freshstart.types.BaseRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SessionRepository extends BaseRepository<Session, Long> {

    @Query("SELECT t FROM Session t where archived = false")
    List<Session> findAll();

    @Query("SELECT t FROM Session t where archived = false")
    List<Session> findAllAsList(Pageable pageable);

    @Query("SELECT count(id) FROM Session where archived = false")
    long count();

    @Query("SELECT t FROM Session t where archived = true")
    List<Session> findAllArchived(Pageable pageable);

    @Query("SELECT count(id) FROM Session where archived = true")
    long countArchived();

    List<Session> findByClients(Client client);
}
