package ca.freshstart.data.healthTable.repository;

import ca.freshstart.data.healthTable.entity.HtRowItem;
import ca.freshstart.types.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface HtRowItemRepository extends BaseRepository<HtRowItem, HtRowItem.PK> {

    @Query("SELECT dataItem FROM HtRowItem dataItem WHERE dataItem.id.columnId = :columnId " +
            "AND dataItem.id.dataFK.id.sessionId = :sessionId " +
            "AND dataItem.id.dataFK.id.clientId = :clientId")
    Optional<HtRowItem> findByColumnIdAndSessionIdAndClientId(@Param("sessionId") Long sessionId,
                                                              @Param("clientId") Long clientId,
                                                              @Param("columnId") Long columnId);

}
