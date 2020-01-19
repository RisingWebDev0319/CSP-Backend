package ca.freshstart.data.healthTable.repository;


import ca.freshstart.data.healthTable.entity.HtRow;
import ca.freshstart.types.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HtRowRepository extends BaseRepository<HtRow, HtRow.PK> {

    @Query("SELECT scd FROM HtRow scd WHERE scd.id.sessionId = :sessionId")
    List<HtRow> findBySessionId(@Param("sessionId") Long sessionId);

}
