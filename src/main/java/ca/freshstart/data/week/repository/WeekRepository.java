package ca.freshstart.data.week.repository;

import ca.freshstart.data.week.entity.Week;
import ca.freshstart.types.BaseRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WeekRepository extends BaseRepository<Week, Long> {

    @Query("SELECT t FROM Week t")
    List<Week> findAllAsList(Pageable pageable);

    @Query("SELECT t FROM Week t")
    List<Week> findAll();

    @Query("SELECT count(id) FROM Week")
    long count();
}
