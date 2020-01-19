package ca.freshstart.data.eventTypes.entity;

import ca.freshstart.types.Duration;
import ca.freshstart.types.EntityId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class EventTypes extends EntityId {

    private String name;

    @JsonIgnore
    private boolean archived;
}
