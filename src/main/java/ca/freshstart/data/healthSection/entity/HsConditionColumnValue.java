package ca.freshstart.data.healthSection.entity;


import ca.freshstart.types.EntityId;
import ca.freshstart.data.healthSection.types.HsTableType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Data
@ToString(exclude = "conditionColumns", callSuper = true)
@EqualsAndHashCode(exclude = {"conditionColumns"}, callSuper = true)
public class HsConditionColumnValue extends EntityId {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String defaultFlagColor;

    @ManyToMany()
    @JoinTable(name = "hs_condition_column_value__hs_condition_column",
            joinColumns = @JoinColumn(name = "hs_condition_column_value_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "hs_condition_column_id", referencedColumnName = "id"))
    @JsonIgnore
    private Set<HsConditionColumn> conditionColumns = new HashSet<>();

    public void addConditionColumn(HsConditionColumn conditionColumn) {
        conditionColumns.add(conditionColumn);
    }

    public void removeConditionColumn(HsConditionColumn conditionColumn) {
        conditionColumns.remove(conditionColumn);
    }

    public Set<HsTableType> getSections() {
        return conditionColumns
                .stream()
                .map(HsConditionColumn::getSectionType)
                .collect(Collectors.toSet());
    }

}
