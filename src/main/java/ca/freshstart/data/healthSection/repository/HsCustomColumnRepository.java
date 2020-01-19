package ca.freshstart.data.healthSection.repository;


import ca.freshstart.data.healthSection.entity.HsCustomColumn;
import ca.freshstart.data.healthSection.types.HsTableType;
import ca.freshstart.types.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HsCustomColumnRepository extends BaseRepository<HsCustomColumn, Long> {

    @Query("SELECT column FROM HsCustomColumn column WHERE column.sectionType = :sectionType")
    List<HsCustomColumn> findBySectionType(@Param("sectionType") HsTableType sectionType);

    @Query("SELECT column FROM HsCustomColumn column WHERE column.sectionType = :sectionType" +
            " AND column.id = :id")
    Optional<HsCustomColumn> findBySectionTypeAndId(@Param("sectionType") HsTableType sectionType,
                                                    @Param("id") Long id);

}
