package ca.freshstart.data.reconcile.entity;

import ca.freshstart.data.concreteEvent.entity.ConcreteEvent;
import ca.freshstart.data.reconcile.types.ConcreteEventEstimateState;
import ca.freshstart.data.reconcile.types.ConcreteEventReconcileState;
import ca.freshstart.types.EntityId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Data
@ToString(callSuper = true, exclude = {"audit", "reconcile", "estimate"})
@EqualsAndHashCode(callSuper = true, exclude = {"audit", "reconcile", "estimate"})
public class ConcreteEventReconcile extends EntityId {

    @OneToOne(fetch = FetchType.EAGER)
    @MapsId
    private ConcreteEvent concreteEvent;

    @Column(nullable = false)
    private ConcreteEventReconcileState reconcileState = ConcreteEventReconcileState.none;

    @Column(nullable = false)
    private ConcreteEventEstimateState estimateState = ConcreteEventEstimateState.none;

    /**
     * cost for client in CAD
     */
    private Float cost;

    @ManyToOne
    @JoinColumn(name = "audit_id")
    @JsonIgnore
    private Audit audit;

    @ManyToOne
    @JoinColumn(name = "reconcile_id")
    @JsonIgnore
    private Reconcile reconcile;

    @ManyToOne
    @JoinColumn(name = "estimate_id")
    @JsonIgnore
    private Estimate estimate;

    public ConcreteEventReconcile() {
    }

    public ConcreteEventReconcile(ConcreteEvent concreteEvent, Float cost) {
        this.concreteEvent = concreteEvent;
        this.cost = cost;
    }

    public Long getEstimateId() {
        return estimate != null
                ? estimate.getId()
                : null;
    }

    public Long getEstimateExternalId() {
        return estimate != null
                ? estimate.getExternalId()
                : null;
    }

}
