package ca.freshstart.data.restriction.repository;

import ca.freshstart.data.restriction.entity.Restriction;
import ca.freshstart.types.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RestrictionRepository extends BaseRepository<Restriction, Long> {
    @Query("SELECT r FROM Restriction r where " +
            "exists (SELECT 1 from r.equipments e where e.id = :equipmentId)")
    List<Restriction> findByEquipmentId(@Param("equipmentId") Long equipmentId);

    @Query("SELECT r FROM Restriction r where " +
            "exists (SELECT 1 from r.rooms e where e.id = :roomId)")
    List<Restriction> findByRoomId(@Param("roomId") Long roomId);
}