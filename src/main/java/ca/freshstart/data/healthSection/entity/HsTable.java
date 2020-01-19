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
@EqualsAndHashCode(exclude = {"columns"}, callSuper = true)
public class HsTable extends EntityId {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private HsTableType type;

    @OneToMany(mappedBy = "table", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @OrderBy("position ASC")
    private Set<HsColumn> columns = new HashSet<>();

    public void addColumn(HsColumn column){
        columns.add(column);
        column.setTable(this);
    }

    public void removeColumn(HsColumn column){
        column.setTable(null);
        columns.remove(column);
    }
}
