package ca.freshstart.data.healthSection.entity;

import ca.freshstart.types.EntityId;
import ca.freshstart.data.healthSection.types.HsTableType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(exclude = "values", callSuper = true)
public class HsSelectColumn extends EntityId {

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private HsTableType sectionType;

    @OneToMany(mappedBy = "selectColumn", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SELECT)
    @OrderBy("position ASC")
    private List<HsSelectColumnValue> values = new ArrayList<>();

    public void addValue(HsSelectColumnValue value) {
        values.add(value);
        value.setSelectColumn(this);
    }

    public void removeValue(HsSelectColumnValue value) {
        value.setSelectColumn(null);
        values.remove(value);
    }
}
