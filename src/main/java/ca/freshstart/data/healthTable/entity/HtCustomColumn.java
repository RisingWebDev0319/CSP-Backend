package ca.freshstart.data.healthTable.entity;

import ca.freshstart.types.EntityId;
import ca.freshstart.data.healthTable.types.HealthTableCustomColumnType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(exclude = "values", callSuper = true)
public class HtCustomColumn extends EntityId{

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private HealthTableCustomColumnType type;

    @OneToMany(mappedBy = "customColumn", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<HtCustomColumnValue> values = new ArrayList<>();

    public void addValue(HtCustomColumnValue value) {
        values.add(value);
        value.setCustomColumn(this);
    }

    public void removeValue(HtCustomColumnValue value) {
        value.setCustomColumn(null);
        values.remove(value);
    }

}
