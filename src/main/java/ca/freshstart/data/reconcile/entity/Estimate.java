package ca.freshstart.data.reconcile.entity;

import ca.freshstart.types.EntityId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@ToString(callSuper = true, exclude = {"events"})
@EqualsAndHashCode(callSuper = true, exclude = {"events"})
public class Estimate extends EntityId {

    private Long externalId;

    @Column(nullable = false)
    private Long clientId;

    private boolean sent;

    @OneToMany(mappedBy = "estimate", fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    private Set<ConcreteEventReconcile> events = new HashSet<>();

}
