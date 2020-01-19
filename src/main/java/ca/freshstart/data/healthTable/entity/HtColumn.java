package ca.freshstart.data.healthTable.entity;

import ca.freshstart.types.EntityId;
import ca.freshstart.data.healthTable.types.HealthTableColumnType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Entity
@Data
@EqualsAndHashCode(exclude = {"customColumn"}, callSuper = true)
public class HtColumn extends EntityId {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private HealthTableColumnType type;

    /**
     * Use null value to mark column as hidden.
     */
    private Integer position;

    /**
     * Whether values of the column edited?.
     */
    @Column(nullable = false)
    private boolean editable = true;

    /**
     * If permanent the column can't be removed or be hidden.
     */
    @Column(nullable = false)
    private boolean permanent = false;

    @ManyToOne
    @JoinColumn(name = "custom_column_id")
    private HtCustomColumn customColumn;

}
