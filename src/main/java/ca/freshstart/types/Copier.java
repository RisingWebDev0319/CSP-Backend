package ca.freshstart.types;

@FunctionalInterface
public interface Copier<T> {
    void copy(T to, T from);
}
