package ca.freshstart.data.suggestions.entity;

import ca.freshstart.types.EntityId;
import ca.freshstart.data.suggestions.types.SuggestedServicesCustomColumnType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@ToString
@EqualsAndHashCode(exclude = "values", callSuper = true)
public class SsCustomColumn extends EntityId {

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SuggestedServicesCustomColumnType type;

    @OneToMany(mappedBy = "customColumn", fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
    private List<SsCustomColumnValue> values = new ArrayList<>();

    public void addValue(SsCustomColumnValue value) {
        values.add(value);
        value.setCustomColumn(this);
    }

    public void removeValue(SsCustomColumnValue value) {
        value.setCustomColumn(null);
        values.remove(value);
    }
}
