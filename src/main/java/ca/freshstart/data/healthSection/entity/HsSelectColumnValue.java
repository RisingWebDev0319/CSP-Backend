package ca.freshstart.data.healthSection.entity;


import ca.freshstart.types.EntityId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@Data
@ToString(exclude = "selectColumn", callSuper = true)
@EqualsAndHashCode(exclude = {"selectColumn"}, callSuper = true)
public class HsSelectColumnValue extends EntityId {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private long value;

    private int position;

    @Column(nullable = false)
    private boolean permanent = false;

    @ManyToOne()
    @JoinColumn(name = "hs_select_column_id")
    @JsonIgnore
    private HsSelectColumn selectColumn;

}
