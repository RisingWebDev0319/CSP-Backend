package ca.freshstart.data.serviceCategory.entity;

import ca.freshstart.types.EntityId;
import ca.freshstart.data.service.entity.Service;
import ca.freshstart.data.therapist.entity.Therapist;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@ToString(exclude = {"therapists"})
@EqualsAndHashCode(exclude = {"therapists"}, callSuper = true)
public class ServiceCategory extends EntityId {

    @Column(unique = true,length = 255)
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @JoinTable(name="services_categories_href",
            joinColumns={@JoinColumn(name="service_category_id")},
            inverseJoinColumns={@JoinColumn(name="service_id")})
    private Set<Service> services = new HashSet<>();

    @ManyToMany(mappedBy="serviceCategories", fetch = FetchType.EAGER)
    @JsonIgnore
    private Set<Therapist> therapists;
}