package ca.freshstart.types;


import java.io.Serializable;
import java.util.List;

public interface ExternalIdRepositoryIf<T, ID extends Serializable> {
    List<T> findByExternalIds(List<ID> ids);
    <S extends T> Iterable<S> save(Iterable<S> entities);
    <S extends T> S save(S entity);
}
