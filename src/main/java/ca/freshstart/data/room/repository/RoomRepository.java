package ca.freshstart.data.room.repository;

import ca.freshstart.data.room.entity.Room;
import ca.freshstart.types.BaseRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends BaseRepository<Room, Long> {

    @Query("SELECT t FROM Room t where archived = false")
    List<Room> findAll();

    @Query("SELECT t FROM Room t where archived = false")
    List<Room> findAllAsList(Pageable pageable);

    @Query("SELECT count(id) FROM Room where archived = false")
    long count();

    @Query("SELECT r FROM Room r WHERE archived = true AND r.name = :name")
    Optional<Room> findArchivedByName(@Param("name")  String name);

}