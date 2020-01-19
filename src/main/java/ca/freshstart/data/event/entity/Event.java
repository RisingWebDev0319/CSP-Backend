package ca.freshstart.data.event.entity;

import ca.freshstart.data.eventTypes.entity.EventTypes;
import ca.freshstart.data.taxes.entity.Taxes;
import ca.freshstart.types.Duration;
import ca.freshstart.types.ExternalIdIf;
import ca.freshstart.types.Updateable;
import ca.freshstart.types.EntityId;
import ca.freshstart.helpers.remote.types.EventRemote;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Transactional
public class Event extends EntityId implements ExternalIdIf<Long>, Updateable<EventRemote> {

    private String name;

    @JsonIgnore
    private boolean archived;

    @Column(unique = true)
    @JsonIgnore
    private Long externalId;

    @Column(name="billing_description")
    private String billingDescription;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "prep", column = @Column(name = "time_prep")),
            @AttributeOverride(name = "processing", column = @Column(name = "time_processing")),
            @AttributeOverride(name = "clean", column = @Column(name = "time_clean"))
    })
    private Duration time;

    private Float price;

    private String client_goals;

    private String therapist_goals;

    @Column(name = "price_purchase")
    private Float pricePurchase;

    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name = "event_type_id")
    private EventTypes eventType;

    private boolean internal;

    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name = "tax_id")
    private Taxes tax;

    public Event() {
    }

    public Event(EventRemote remote) {
        externalId = remote.getId();
        update(remote);
    }

    public void update(EventRemote remote) {
        if (remote == null) {
            return;
        }

        setName(remote.getName());
        setTime(new Duration() {{
            setPrep(remote.getPrepTime());
            setProcessing(remote.getDuration());
            setClean(remote.getCleanTime());
        }});
        setPrice(remote.getPrice());
    }

    @Transient
    public Float getSalePrice() {
        return this.price;
    }

    @Transient
    public Float getPurchasePrice() {
        return this.pricePurchase == null ? 0 : this.pricePurchase;
    }

    public String getName() {
        return name;
    }
}
