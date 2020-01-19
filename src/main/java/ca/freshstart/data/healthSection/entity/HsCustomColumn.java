package ca.freshstart.data.healthSection.entity;

import ca.freshstart.types.EntityId;
import ca.freshstart.data.healthSection.types.HsCustomColumnType;
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
public class HsCustomColumn extends EntityId {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private HsCustomColumnType type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private HsTableType sectionType;

    @OneToMany(mappedBy = "customColumn", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SELECT)
    @OrderBy("position ASC")
    private List<HsCustomColumnValue> values = new ArrayList<>();

    public void addValue(HsCustomColumnValue value) {
        values.add(value);
        value.setCustomColumn(this);
    }

    public void removeValue(HsCustomColumnValue value) {
        value.setCustomColumn(null);
        values.remove(value);
    }
}
