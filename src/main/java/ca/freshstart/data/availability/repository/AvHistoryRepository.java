package ca.freshstart.data.availability.repository;

import ca.freshstart.data.availability.entity.AvHistoryRecord;
import ca.freshstart.types.BaseRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface AvHistoryRepository extends BaseRepository<AvHistoryRecord, Long> {

    @Query("SELECT t FROM AvHistoryRecord t")
    List<AvHistoryRecord> findAll();

    @Query("SELECT t FROM AvHistoryRecord t where t.editDate >= :startDate " +
            "and t.editDate <= :endDate and t.therapistId = :therapistId")
    List<AvHistoryRecord> findAll(Pageable pageable,
                                  @Param("therapistId") Long therapistId,
                                  @Param("startDate") Date startDate,
                                  @Param("endDate") Date endDate);

    @Query("SELECT count(id) FROM AvHistoryRecord t where t.editDate >= :startDate " +
            "and t.editDate <= :endDate and t.therapistId = :therapistId")
    long count(@Param("therapistId") Long therapistId,
               @Param("startDate") Date startDate,
               @Param("endDate") Date endDate);
}