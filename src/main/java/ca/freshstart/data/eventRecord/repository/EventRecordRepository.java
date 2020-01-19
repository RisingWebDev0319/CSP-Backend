package ca.freshstart.data.eventRecord.repository;

import ca.freshstart.data.eventRecord.entity.EventRecord;
import ca.freshstart.types.BaseRepository;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface EventRecordRepository extends BaseRepository<EventRecord, Long> {

    List<EventRecord> findAll(Specification<EventRecord> spec);
}