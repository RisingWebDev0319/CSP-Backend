package ca.freshstart.data.healthSection.repository;


import ca.freshstart.data.healthSection.entity.HsColumn;
import ca.freshstart.data.healthSection.types.HsColumnType;
import ca.freshstart.data.healthSection.types.HsTableType;
import ca.freshstart.types.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HsColumnRepository extends BaseRepository<HsColumn, Long> {

    @Query("SELECT column FROM HsColumn column" +
            " WHERE column.table.type = :sectionType" +
            " AND column.position IS NOT NULL" +
            " ORDER BY column.position ASC")
    List<HsColumn> findPositionedBySectionType(@Param("sectionType") HsTableType sectionType);

    @Query("SELECT column FROM HsColumn column" +
            " WHERE column.table.type = :sectionType" +
            " AND column.position IS NOT NULL" +
            " AND column.type = :columnType" +
            " AND column.permanent = false")
    List<HsColumn> findPositionedBySectionTypeAndTypeAndNotPermanent(@Param("sectionType") HsTableType sectionType,
                                                                     @Param("columnType") HsColumnType columnType);

    @Query("SELECT column FROM HsColumn column" +
            " WHERE column.table.type = :sectionType" +
            " AND column.type = :columnType")
    List<HsColumn> findBySectionTypeAndType(@Param("sectionType") HsTableType sectionType,
                                            @Param("columnType") HsColumnType columnType);

    @Query("SELECT column FROM HsColumn column" +
            " WHERE column.table.type = :sectionType" +
            " AND column.title = :columnTitle")
    List<HsColumn> findBySectionTypeAndTitle(@Param("sectionType") HsTableType sectionType,
                                             @Param("columnTitle") String columnTitle);

    @Query("SELECT column FROM HsColumn column" +
            " WHERE column.table.type = :sectionType" +
            " AND column.id = :id")
    Optional<HsColumn> findBySectionTypeAndId(@Param("sectionType") HsTableType sectionType,
                                              @Param("id") long id);

    @Query("SELECT column FROM HsColumn column" +
            " WHERE column.position IS NULL" +
            " AND column.table.type = :sectionType" +
            " AND column.type = :columnType" +
            " AND column.customColumn.id = :customColumnId" +
            " ORDER BY column.id ASC")
    List<HsColumn> findNotPositionedBySectionTypeAndTypeAndCustomColumnId(
            @Param("sectionType") HsTableType sectionType,
            @Param("columnType") HsColumnType columnType,
            @Param("customColumnId") Long customColumnId);

    @Query("SELECT column FROM HsColumn column" +
            " WHERE column.position IS NULL" +
            " AND column.table.type = :sectionType" +
            " AND column.type = :columnType" +
            " ORDER BY column.id ASC")
    List<HsColumn> findNotPositionedBySectionTypeAndType(
            @Param("sectionType") HsTableType sectionType,
            @Param("columnType") HsColumnType columnType);

}
