package ca.freshstart.data.taxes.repository;

import ca.freshstart.data.taxes.entity.Taxes;
import ca.freshstart.types.BaseRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaxesRepository extends BaseRepository<Taxes,Long>{
    @Query("SELECT t FROM Taxes t where archived = false")
    List<Taxes> findAll();

    @Query("SELECT t FROM Taxes t where archived = false")
    List<Taxes> findAllAsList(Pageable pageable);

    @Query("SELECT count(id) FROM Taxes where archived = false")
    long count();

    @Query("SELECT r FROM Taxes r WHERE archived = false AND r.code = :code")
    List<Taxes> findByCode(@Param("code")  String code);
}
