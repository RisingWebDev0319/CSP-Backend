package ca.freshstart.data.healthSection.repository;


import ca.freshstart.data.healthSection.entity.HsTable;
import ca.freshstart.data.healthSection.types.HsTableType;
import ca.freshstart.types.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface HsTableRepository extends BaseRepository<HsTable, Long> {

    @Query("SELECT table FROM HsTable table WHERE table.type = :sectionType")
    Optional<HsTable> findBySectionType(@Param("sectionType") HsTableType sectionType);

}
