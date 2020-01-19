package ca.freshstart.data.equipment.repository;

import ca.freshstart.data.equipment.entity.Equipment;
import ca.freshstart.types.BaseRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EquipmentRepository extends BaseRepository<Equipment, Long> {

    @Query("SELECT e FROM Equipment e WHERE id = :id")
    Optional<Equipment> findById(@Param("id") Long id);

    @Query("SELECT t FROM Equipment t where archived = false")
    List<Equipment> findAll();

    @Query("SELECT e FROM Equipment e WHERE id IN (:ids)")
    Set<Equipment> findByIds(@Param("ids") List<Long> ids);

    @Query("SELECT t FROM Equipment t where archived = false")
    List<Equipment> findAllAsList(Pageable pageable);

    @Query("SELECT count(id) FROM Equipment where archived = false")
    long count();

    @Query("SELECT e FROM Equipment e WHERE e.archived = true AND e.name = :name")
    Optional<Equipment> findArchivedByName(@Param("name")  String name);

}