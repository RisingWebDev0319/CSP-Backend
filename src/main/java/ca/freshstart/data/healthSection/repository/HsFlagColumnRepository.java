package ca.freshstart.data.healthSection.repository;


import ca.freshstart.data.healthSection.entity.HsFlagColumn;
import ca.freshstart.data.healthSection.types.HsTableType;
import ca.freshstart.types.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HsFlagColumnRepository extends BaseRepository<HsFlagColumn, Long> {

    @Query("SELECT column FROM HsFlagColumn column WHERE column.sectionType = :sectionType")
    List<HsFlagColumn> findBySectionType(@Param("sectionType") HsTableType sectionType);

}
