package ca.freshstart.data.healthSection.entity;


import ca.freshstart.types.EntityId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Data
@ToString(exclude = "flagColumn", callSuper = true)
@EqualsAndHashCode(exclude = {"flagColumn"}, callSuper = true)
public class HsFlagColumnValue extends EntityId {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false)
    private boolean permanent = false;

    @ManyToOne()
    @JoinColumn(name = "hs_flag_column_id")
    @JsonIgnore
    private HsFlagColumn flagColumn;

}
