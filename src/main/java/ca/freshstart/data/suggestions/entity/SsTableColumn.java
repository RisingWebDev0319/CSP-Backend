package ca.freshstart.data.suggestions.entity;

import ca.freshstart.types.EntityId;
import ca.freshstart.data.suggestions.types.SuggestedServicesColumnType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class SsTableColumn extends EntityId {

    private String title;

    @Enumerated(EnumType.STRING)
    private SuggestedServicesColumnType type;

    @ManyToOne
//    @JoinColumn(name = "ss_custom_column_id")
    private SsCustomColumn customColumn;
}
