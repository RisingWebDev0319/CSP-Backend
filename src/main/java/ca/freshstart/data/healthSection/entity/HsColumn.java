package ca.freshstart.data.healthSection.entity;

import ca.freshstart.types.EntityId;
import ca.freshstart.data.healthSection.types.HsColumnType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Data
@ToString(exclude = "table", callSuper = true)
@EqualsAndHashCode(exclude = {"table", "customColumn", "selectColumn"}, callSuper = true)
public class HsColumn extends EntityId {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private HsColumnType type;

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
    @JoinColumn(name = "hs_table_id")
    @JsonIgnore
    private HsTable table;

    @ManyToOne
    @JoinColumn(name = "custom_column_id")
    private HsCustomColumn customColumn;

    @ManyToOne
    @JoinColumn(name = "select_column_id")
    private HsSelectColumn selectColumn;

}
