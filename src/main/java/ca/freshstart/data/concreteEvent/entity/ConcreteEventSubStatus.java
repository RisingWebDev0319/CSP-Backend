package ca.freshstart.data.concreteEvent.entity;


import ca.freshstart.types.EntityId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.Entity;

@Entity
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ConcreteEventSubStatus extends EntityId {
    private String name;
}
