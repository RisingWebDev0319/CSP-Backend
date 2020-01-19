package ca.freshstart.applications.client.helpers;

import ca.freshstart.data.availability.entity.AvTherapistDayRecord;
import ca.freshstart.data.client.entity.Client;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.Date;
import java.util.List;

import static ca.freshstart.helpers.CspUtils.isNullOrEmpty;

public class CspSpecifications {
    public static Specification<Client> findClients(String sort, String searchField, String searchString) {
        return new Specification<Client>() {
            public Predicate toPredicate(Root<Client> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
                if(!isNullOrEmpty(sort)) {
                    boolean asc = sort.startsWith("+");
                    String field = "name";

                    if(asc) {
                        query = query.orderBy(builder.asc(root.get(field)));
                    } else {
                        query = query.orderBy(builder.desc(root.get(field)));
                    }
                }

                query.where(builder.equal(root.get("archived"), false));

                query.where(builder.like(builder.lower(root.get(searchField)), "%" + searchString.toLowerCase() + "%"));

                return query.getRestriction();
            }
        };
    }

    public static Specification<AvTherapistDayRecord> findAvailability(Long therapistId, Date dateFrom, Date dateTo) {
        return new Specification<AvTherapistDayRecord>() {
            public Predicate toPredicate(Root<AvTherapistDayRecord> root, CriteriaQuery<?> query,
                                         CriteriaBuilder builder) {

                query.where(builder.equal(root.get("therapist_id"), therapistId));

                query.where(builder.greaterThan(root.get("date"), dateFrom));

                query.where(builder.lessThan(root.get("date"), dateTo));

                return query.getRestriction();
            }
        };
    }

    public static Specification<AvTherapistDayRecord> findAvailabilityInDates(Long therapistId, List<Date> dates) {
        return new Specification<AvTherapistDayRecord>() {
            public Predicate toPredicate(Root<AvTherapistDayRecord> root, CriteriaQuery<?> query,
                                         CriteriaBuilder builder) {

                query.where(builder.equal(root.get("therapist_id"), therapistId));

                query.where(root.get("date").in(dates));

                return query.getRestriction();
            }
        };
    }
}