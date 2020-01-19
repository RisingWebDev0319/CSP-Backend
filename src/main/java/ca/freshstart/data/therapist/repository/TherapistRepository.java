package ca.freshstart.data.therapist.repository;

import ca.freshstart.data.therapist.entity.Therapist;
import ca.freshstart.types.ExternalIdRepositoryIf;
import ca.freshstart.types.BaseRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface TherapistRepository extends BaseRepository<Therapist, Long>, ExternalIdRepositoryIf<Therapist, String> {

    @Modifying
    @Transactional
    @Query("UPDATE Therapist SET archived = :archived")
    void updateArchiveAll(@Param("archived") boolean archived);

    @Query("SELECT t FROM Therapist t WHERE externalId IN (:ids)")
    List<Therapist> findByExternalIds(@Param("ids") List<String> ids);

    @Query("SELECT t FROM Therapist t where archived = false")
    List<Therapist> findAll();

    @Query("SELECT t FROM Therapist t where t.archived = false")
    List<Therapist> findAllAsList(Pageable pageable);

    @Query("SELECT t FROM Therapist t WHERE t.serviceCategories IS EMPTY AND t.services IS EMPTY AND t.events IS EMPTY AND archived = false")
    List<Therapist> findAllUnassigned(Pageable pageable);

    @Query("SELECT count(*) FROM Therapist t WHERE t.serviceCategories IS EMPTY or t.services IS EMPTY and archived = false")
    long countUnassigned();

    @Query("SELECT t FROM Therapist t WHERE t.email not in (select email from AppUser) and archived = false")
    List<Therapist> findAllNoAccount(Pageable pageable);

    @Query("SELECT count(id) FROM Therapist t WHERE t.email not in (select email from AppUser) and archived = false")
    long countNoAccount();

    @Query("SELECT count(id) FROM Therapist where archived = false")
    long count();

    @Query("SELECT t FROM Therapist t WHERE t.email = :email")
    Optional<Therapist> findByEmail(@Param("email") String email);

//    @Query("SELECT COUNT(id) > 0 FROM Therapist t WHERE t.email = :email")
//    boolean isEmailExist(@Param("email") String email);

    boolean existsTherapistByEmail(String email);

    @Transactional
    <S extends Therapist> Iterable<S> save(Iterable<S> entities);
    // http://docs.spring.io/spring-data/jpa/docs/1.6.5.RELEASE/reference/html/repositories.html

    // -----------------------------------------------------------------------------------------------------------------

//    @Query("SELECT t.dayRecords FROM Therapist t where id = :id")
//    List<AvailabilityTherapistDayRecord> findDayRecords(Long therapistId);
}
