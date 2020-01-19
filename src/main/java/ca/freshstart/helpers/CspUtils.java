package ca.freshstart.helpers;

import ca.freshstart.types.EntityId;
import ca.freshstart.types.Copier;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CspUtils {
    public static String REMOTE_SERVER_JSON_RE = "null\\((.*)\\)";
    public static int REMOTE_SERVER_JSON_RE_GROUP = 1;
    public static Pattern REMOTE_SERVER_JSON_PATTERN = Pattern.compile(REMOTE_SERVER_JSON_RE);

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isNullOrEmpty(Collection col) {
        return col == null || col.size() == 0;
    }

    public static boolean isObjectsEquals(Object o1, Object o2) {
        return (o1 == null && o2 == null) || (o1 != null && o2 != null && o1.equals(o2));
    }

    public static String urlToString(String url) {
        try {
            String s = IOUtils.toString(new URL(url));

            Matcher m = REMOTE_SERVER_JSON_PATTERN.matcher(s);
            if (m.find()) {
                return m.group(REMOTE_SERVER_JSON_RE_GROUP);
            }

            return s;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Sort createSort(String sortParameter) {
        if (isNullOrEmpty(sortParameter)) {
            return null;
        }

        Sort.Direction direction = sortParameter.startsWith("+") ? Sort.Direction.ASC : Sort.Direction.DESC;

        String property = sortParameter.substring(1);

        return new Sort(new Sort.Order(direction, property).nullsLast());
    }

    public static <T extends EntityId> void mergeValues(List<T> mergeToList,
                                                        List<T> mergeFromList,
                                                        Consumer<? super T> removeValue,
                                                        Consumer<? super T> addValue,
                                                        Copier<T> copier) {

        // remove
        List<T> valuesToDelete = mergeToList.stream()
                .filter(prevValue -> {
                    long prevValueId = prevValue.getId();
                    boolean present = mergeFromList
                            .stream()
                            .anyMatch(newValue -> newValue.getId() != null && newValue.getId() == prevValueId);
                    return !present;
                })
                .collect(Collectors.toList());

        valuesToDelete.forEach(removeValue);

        // update
        mergeToList.forEach(toValue -> {
            long toValueId = toValue.getId();

            mergeFromList.stream()
                    .filter(fromValue -> fromValue.getId() != null && fromValue.getId() == toValueId)
                    .findFirst()
                    .ifPresent(fromValue -> {
                        copier.copy(toValue, fromValue);
                    });
        });

        // add new
        mergeFromList.stream()
                .filter(newValue -> newValue.getId() == null)
                .forEach(addValue);

    }

    public static <C, N> CollectionDiffResult<C, N> diffCollections(Collection<C> collectionPrev,
                                                                    Collection<N> collectionNew,
                                                                    BiPredicate<? super C, ? super N> checkIdentity) {
        List<C> removed = collectionPrev.stream()
                .filter(currValue ->
                        collectionNew.stream().noneMatch(newValue ->
                                checkIdentity.test(currValue, newValue)))
                .collect(Collectors.toList());

        List<N> added = collectionNew.stream()
                .filter(newValue ->
                        collectionPrev.stream().noneMatch(currValue ->
                                checkIdentity.test(currValue, newValue)))
                .collect(Collectors.toList());

        List<C> notMoved = collectionPrev.stream()
                .filter(currValue ->
                        collectionNew.stream().anyMatch(newValue ->
                                checkIdentity.test(currValue, newValue)))
                .collect(Collectors.toList());

        return new CollectionDiffResult<>(removed, added, notMoved);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CollectionDiffResult<C, N> {
        private List<C> removed;
        private List<N> added;
        private List<C> notMoved;
    }

}