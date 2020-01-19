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
@EqualsAndHashCode(exclude = {"values"}, callSuper = true)
public class HsRow extends EntityId {

    @Column(name = "client_id", nullable = false, updatable = false)
    private long clientId;

    @Column(name = "session_id", nullable = false, updatable = false)
    private long sessionId;

    @Column(nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private HsTableType section;

    @OneToMany(mappedBy = "id.row", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<HsRowItem> values = new HashSet<>();

    public void addValue(HsRowItem value){
        values.add(value);
        value.setRow(this);
    }

    public void removeValue(HsRowItem value){
        value.setRow(null);
        values.remove(value);
    }
}
