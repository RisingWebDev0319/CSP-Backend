package ca.freshstart.data.healthSection.repository;


import ca.freshstart.data.healthSection.entity.HsConditionColumn;
import ca.freshstart.data.healthSection.types.HsTableType;
import ca.freshstart.types.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface HsConditionColumnRepository extends BaseRepository<HsConditionColumn, Long> {

    @Query("SELECT column FROM HsConditionColumn column WHERE column.sectionType = :sectionType")
    List<HsConditionColumn> findBySectionType(@Param("sectionType") HsTableType sectionType);

    @Query("SELECT column FROM HsConditionColumn column" +
            " WHERE column.sectionType IN (:sectionTypes)")
    List<HsConditionColumn> findBySectionTypes(@Param("sectionTypes") Set<HsTableType> sectionTypes);

}
