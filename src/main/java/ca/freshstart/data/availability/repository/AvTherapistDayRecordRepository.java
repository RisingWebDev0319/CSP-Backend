package ca.freshstart.data.availability.repository;

import ca.freshstart.data.availability.entity.AvTherapistDayRecord;
import ca.freshstart.types.BaseRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface AvTherapistDayRecordRepository extends BaseRepository<AvTherapistDayRecord, Long> {

    @Query("SELECT record FROM AvTherapistDayRecord record where record.therapist.id = :therapistId")
    List<AvTherapistDayRecord> findByTherapistId(@Param("therapistId") Long therapistId);

    @Query("SELECT record FROM AvTherapistDayRecord record where record.therapist.id = :therapistId")
    List<AvTherapistDayRecord> findAll(@Param("therapistId") Long therapistId, Pageable pageable);

    @Query("SELECT record FROM AvTherapistDayRecord record where record.therapist.id = :therapistId and " +
            " record.date BETWEEN :dateFrom AND :dateTo")
    List<AvTherapistDayRecord> findBetweenDates(@Param("therapistId") Long therapistId,
                                                @Param("dateFrom") Date dateFrom,
                                                @Param("dateTo") Date dateTo);

    @Query("SELECT record FROM AvTherapistDayRecord record where record.therapist.id = :therapistId and record.date = :date")
    Optional<AvTherapistDayRecord> findByDate(@Param("therapistId") Long therapistId, @Param("date") Date date);

    @Query("SELECT record FROM AvTherapistDayRecord record " +
            "where record.therapist.id = :therapistId and record.date IN (:dates)")
    List<AvTherapistDayRecord> findWithDates(@Param("therapistId") Long therapistId,
                                             @Param("dates") List<Date> dates);
}