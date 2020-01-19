package ca.freshstart.data.appUser.repository;

import ca.freshstart.data.appUser.entity.AppUser;
import ca.freshstart.types.BaseRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends BaseRepository<AppUser, Long> {

    @Query("SELECT u FROM AppUser u where id <> 1")
    List<AppUser> findAllAsList(Pageable pageable);

    @Query("SELECT u FROM AppUser u WHERE LOWER(u.email) = LOWER(:email) and LOWER(u.password) = LOWER(:password) and locked = false")
    Optional<AppUser> find(@Param("email") String email, @Param("password") String password);

    @Query("SELECT u FROM AppUser u where id <> 1")
    List<AppUser> findAll();

    @Query("SELECT count(id) FROM AppUser where id <> 1")
    long count();

    @Query("SELECT u FROM AppUser u WHERE LOWER(u.email) = LOWER(:email) and locked = false")
    Optional<AppUser> findByEmail(@Param("email") String email);
}