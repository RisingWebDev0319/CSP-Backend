package ca.freshstart.data.healthSection.repository;


import ca.freshstart.data.healthSection.entity.HsRow;
import ca.freshstart.data.healthSection.types.HsTableType;
import ca.freshstart.types.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HsRowRepository extends BaseRepository<HsRow, Long> {

    @Query("SELECT row FROM HsRow row" +
            " WHERE row.section = :section" +
            " AND row.clientId = :clientId " +
            " AND row.sessionId = :sessionId")
    List<HsRow> findBySectionTypeAndClientIdAndSessionId(@Param("section") HsTableType section,
                                                         @Param("clientId") Long clientId,
                                                         @Param("sessionId") Long sessionId);

    @Query("SELECT row FROM HsRow row" +
            " WHERE row.clientId = :clientId" +
            " AND row.sessionId = :sessionId")
    List<HsRow> findByClientIdAndSessionId(@Param("clientId") Long clientId,
                                           @Param("sessionId") Long sessionId);

}
