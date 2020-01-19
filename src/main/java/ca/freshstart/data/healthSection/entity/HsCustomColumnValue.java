package ca.freshstart.data.healthSection.entity;

import ca.freshstart.types.EntityId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Data
@ToString(exclude = "customColumn", callSuper = true)
@EqualsAndHashCode(exclude = {"customColumn"}, callSuper = true)
public class HsCustomColumnValue extends EntityId {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private long value;

    private int position;

    @Column(nullable = false)
    private boolean permanent = false;

    @ManyToOne
    @JoinColumn(name = "hs_custom_column_id")
    @JsonIgnore
    private HsCustomColumn customColumn;
}
