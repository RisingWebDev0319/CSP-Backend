package ca.freshstart.data.suggestions.repository;

import ca.freshstart.data.suggestions.types.SuggestedServicesColumnType;
import ca.freshstart.data.suggestions.entity.SsTableColumn;
import ca.freshstart.types.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SsTableColumnRepository extends BaseRepository<SsTableColumn, Long> {

    @Query("SELECT t FROM SsTableColumn t where t.customColumn.id = :customColumnId")
    Optional<SsTableColumn> findCustomColumnUsage(@Param("customColumnId") Long customColumnId);

    @Query("SELECT t FROM SsTableColumn t")
    List<SsTableColumn> findAll();

    @Query("SELECT count(id) FROM SsTableColumn")
    long count();

//    ==================================

    @Query("SELECT column FROM SsTableColumn column WHERE column.type = :columnType" +
            " AND column.customColumn.id = :customColumnId" +
            " ORDER BY column.id ASC")
    List<SsTableColumn> findByTypeAndCustomColumnId(@Param("columnType") SuggestedServicesColumnType columnType,
                                                    @Param("customColumnId") Long customColumnId);

    @Query("SELECT column FROM SsTableColumn column WHERE column.type = :columnType" +
            " ORDER BY column.id ASC")
    List<SsTableColumn> findByType(@Param("columnType") SuggestedServicesColumnType columnType);
}