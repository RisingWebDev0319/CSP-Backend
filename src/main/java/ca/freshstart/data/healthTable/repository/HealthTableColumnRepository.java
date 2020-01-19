package ca.freshstart.data.healthTable.repository;

import ca.freshstart.data.healthTable.entity.HtColumn;
import ca.freshstart.data.healthTable.types.HealthTableColumnType;
import ca.freshstart.types.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HealthTableColumnRepository extends BaseRepository<HtColumn, Long> {

    @Query("SELECT column FROM HtColumn column WHERE column.position IS NOT NULL" +
            " ORDER BY column.position ASC")
    List<HtColumn> findPositioned();

    @Query("SELECT column FROM HtColumn column WHERE column.position IS NULL" +
            " AND column.type = :columnType" +
            " AND column.customColumn.id = :customColumnId" +
            " ORDER BY column.id ASC")
    List<HtColumn> findNotPositionedByTypeAndCustomColumnId(@Param("columnType") HealthTableColumnType columnType,
                                                            @Param("customColumnId") Long customColumnId);

    @Query("SELECT column FROM HtColumn column WHERE column.position IS NULL" +
            " AND column.type = :columnType" +
            " ORDER BY column.id ASC")
    List<HtColumn> findNotPositionedByType(@Param("columnType") HealthTableColumnType columnType);

    @Query("SELECT column FROM HtColumn column WHERE column.type = :columnType" +
            " ORDER BY column.id ASC")
    List<HtColumn> findByType(@Param("columnType") HealthTableColumnType columnType);
}
