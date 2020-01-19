package ca.freshstart.data.mails.repository;
import ca.freshstart.data.mails.entity.Mails;
import ca.freshstart.types.BaseRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MailsRepository extends BaseRepository<Mails, Long> {
    @Query("SELECT e FROM Mails e WHERE type.key = :type")
    Mails findByType(@Param("type") String type) throws DataIntegrityViolationException;
}