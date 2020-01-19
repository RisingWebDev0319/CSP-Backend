package ca.freshstart.data.suggestions.entity;

import ca.freshstart.types.EntityId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Data
@ToString(exclude = "customColumn")
@EqualsAndHashCode(exclude = {"customColumn"}, callSuper = true)
public class SsCustomColumnValue extends EntityId {

    @Column(name = "column_value")
    private Long value;

    private String title;

    @Column(name = "column_order")
    private Long order;

    @ManyToOne
    @JsonIgnore
    private SsCustomColumn customColumn;
}
