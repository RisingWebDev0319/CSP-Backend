package ca.freshstart.data.restriction.entity;

import ca.freshstart.data.equipment.entity.Equipment;
import ca.freshstart.data.room.entity.Room;
import ca.freshstart.types.RestrictionType;
import ca.freshstart.types.EntityId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.Set;

@Entity
@Data
@ToString
@Table(
        uniqueConstraints=
        @UniqueConstraint(name = "linked_id_type_uk", columnNames={"linkedId", "type"})
)
@EqualsAndHashCode(callSuper = true)
public class Restriction extends EntityId {

    @Column(unique = true, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    private RestrictionType type;

    private Long linkedId;

    @ManyToMany(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @JoinTable(name="restriction_rooms",
            joinColumns={@JoinColumn(name="restriction_id")},
            inverseJoinColumns={@JoinColumn(name="room_id")})
    private Set<Room> rooms;

    @ManyToMany(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @JoinTable(name="restriction_equipments",
            joinColumns={@JoinColumn(name="restriction_id")},
            inverseJoinColumns={@JoinColumn(name="equipment_id")})
    private Set<Equipment> equipments;
}