package ca.freshstart.data.concreteEvent.entity;

import ca.freshstart.types.EntityId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;

/**
 * Change code for ConcreteEvent.
 * Used by Therapist to confirm event change.
 */
@Entity
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ConcreteEventChange extends EntityId {

    private Long concreteEventId;

    @Column(nullable = false)
    private String eventCode;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "previous_value_id", unique = true)
    private ConcreteEventChangeValue preValue;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "new_value_id", unique = true)
    private ConcreteEventChangeValue newValue;
}
