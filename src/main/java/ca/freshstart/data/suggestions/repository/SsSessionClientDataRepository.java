package ca.freshstart.data.suggestions.repository;

import ca.freshstart.data.suggestions.entity.SsSessionClientData;
import ca.freshstart.types.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SsSessionClientDataRepository extends BaseRepository<SsSessionClientData, SsSessionClientData.PK> {

    @Query("SELECT scd FROM SsSessionClientData scd WHERE scd.id.session.id = :sessionId")
    List<SsSessionClientData> findBySessionId(@Param("sessionId") Long sessionId);
}
