package ca.freshstart.data.healthTable.entity;

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
public class HtCustomColumnValue extends EntityId {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private long value;

    private long position;

    @ManyToOne
    @JoinColumn(name = "hs_custom_column_id")
    @JsonIgnore
    private HtCustomColumn customColumn;

}
