package ca.freshstart.data.report.repository;

import ca.freshstart.data.report.entity.Report;
import ca.freshstart.types.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReportRepository  extends BaseRepository<Report,Long>{
    @Query("SELECT c FROM Report c where archived = false")
    List<Report> findAll();

    @Query("SELECT c FROM Report c where archived = false")
    List<Report> findAllAsList(Pageable pageable);

    Page<Report> findAll(Specification<Report> spec, Pageable pageable);

    Optional <Report> findByUrl(String Url);

    @Query("SELECT count(id) FROM Report where archived = false")
    long count();
}
