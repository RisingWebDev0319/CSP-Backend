package ca.freshstart.data.healthSection.entity;

import ca.freshstart.types.EntityId;
import ca.freshstart.data.healthSection.types.HsTableType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@EqualsAndHashCode(exclude = "values", callSuper = true)
public class HsConditionColumn extends EntityId {

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private HsTableType sectionType;

    @ManyToMany(mappedBy = "conditionColumns", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @OrderBy("title ASC")
    private Set<HsConditionColumnValue> values = new HashSet<>();

    public void addValue(HsConditionColumnValue value) {
        values.add(value);
    }

    public void removeValue(HsConditionColumnValue value) {
        values.remove(value);
    }

}

