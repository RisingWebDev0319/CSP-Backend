package ca.freshstart.data.room.entity;

import ca.freshstart.data.restriction.entity.Restriction;
import ca.freshstart.types.EntityId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@ToString(exclude = {"restrictions"})
@EqualsAndHashCode(exclude = {"restrictions"}, callSuper = true)
public class Room extends EntityId {

    @Column(unique = true, length = 255)
    private String name;

    private Long capacity;

    @JsonIgnore
    private boolean archived = false;

    @JsonIgnore
    @ManyToMany(mappedBy="rooms")
    private Set<Restriction> restrictions = new HashSet<>();
}