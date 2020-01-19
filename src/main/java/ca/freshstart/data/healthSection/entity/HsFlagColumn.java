package ca.freshstart.data.healthSection.entity;

import ca.freshstart.types.EntityId;
import ca.freshstart.data.healthSection.types.HsTableType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(exclude = "values", callSuper = true)
public class HsFlagColumn extends EntityId {

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private HsTableType sectionType;

    @OneToMany(mappedBy = "flagColumn", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("color ASC")
    private List<HsFlagColumnValue> values = new ArrayList<>();

    public void addValue(HsFlagColumnValue value){
        values.add(value);
        value.setFlagColumn(this);
    }

    public void removeValue(HsFlagColumnValue value){
        value.setFlagColumn(null);
        values.remove(value);
    }

}

