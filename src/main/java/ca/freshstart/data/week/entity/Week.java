package ca.freshstart.data.week.entity;

import ca.freshstart.data.therapist.entity.Therapist;
import ca.freshstart.types.EntityId;
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
public class Week extends EntityId {


    @Column(unique = true)
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @JoinTable(name="therapist_week",
            joinColumns={@JoinColumn(name="week_id")},
            inverseJoinColumns={@JoinColumn(name="therapist_id")})
    private Set<Therapist> therapists = new HashSet<>();

    public Week() {
    }

}
