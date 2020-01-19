package ca.freshstart.data.room.repository;

import ca.freshstart.data.room.entity.RoomBookedTime;
import ca.freshstart.types.BaseRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RoomBookedRepository extends BaseRepository<RoomBookedTime, Long> {

    @Query("SELECT t FROM RoomBookedTime t where archived = false")
    List<RoomBookedTime> findAll();

    @Query("SELECT count(id) FROM RoomBookedTime where archived = false")
    long count();
}