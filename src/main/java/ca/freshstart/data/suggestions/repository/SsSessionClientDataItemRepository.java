package ca.freshstart.data.suggestions.repository;

import ca.freshstart.data.suggestions.entity.SsSessionClientDataItem;
import ca.freshstart.types.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SsSessionClientDataItemRepository extends BaseRepository<SsSessionClientDataItem, SsSessionClientDataItem.PK> {

    @Query("SELECT dataItem FROM SsSessionClientDataItem dataItem" +
            " WHERE dataItem.id.dataFK.id.session.id = :sessionId" +
            " AND dataItem.id.dataFK.id.client.id = :clientId" +
            " AND dataItem.id.columnId = :columnId")
    Optional<SsSessionClientDataItem> findByColumnIdAndSessionIdAndClientId(@Param("sessionId") Long sessionId,
                                                                            @Param("clientId")  Long clientId,
                                                                            @Param("columnId")  Long columnId);
}
