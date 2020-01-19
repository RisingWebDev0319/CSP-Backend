package ca.freshstart.types;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface BaseRepository<T, ID extends Serializable> extends PagingAndSortingRepository<T, ID> {

    Optional<T> findById(ID id);

    List<T> findAll();

    @Query("SELECT t FROM #{#entityName} t WHERE t.id IN (:ids)")
    List<T> findByIds(@Param("ids") Collection<ID> ids);

    @Query("SELECT count(id) FROM #{#entityName}")
    long countAll();

}