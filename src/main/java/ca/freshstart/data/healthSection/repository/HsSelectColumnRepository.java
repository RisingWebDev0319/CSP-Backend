package ca.freshstart.data.healthSection.repository;


import ca.freshstart.data.healthSection.entity.HsSelectColumn;
import ca.freshstart.data.healthSection.types.HsTableType;
import ca.freshstart.types.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HsSelectColumnRepository extends BaseRepository<HsSelectColumn, Long> {

    @Query("SELECT column FROM HsSelectColumn column WHERE column.sectionType = :sectionType")
    List<HsSelectColumn> findBySectionType(@Param("sectionType") HsTableType sectionType);

}
