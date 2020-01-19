package ca.freshstart.data.service.entity;

import ca.freshstart.data.taxes.entity.Taxes;
import ca.freshstart.types.Duration;
import ca.freshstart.types.ExternalIdIf;
import ca.freshstart.types.Updateable;
import ca.freshstart.types.EntityId;
import ca.freshstart.data.serviceCategory.entity.ServiceCategory;
import ca.freshstart.helpers.remote.types.ServiceRemote;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.Set;

@Entity
@Data
@ToString(exclude = {"categories"})
@EqualsAndHashCode(exclude = {"categories"}, callSuper = true)
public class Service extends EntityId implements ExternalIdIf<Long>, Updateable<ServiceRemote> {

    private String name;

    @Column(unique = true)
    @JsonIgnore
    private Long externalId;

    @JsonIgnore
    private boolean archived;

    @Column(name = "billing_description")
    private String billingDescription;

    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name = "tax_id")
    private Taxes tax;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "prep", column = @Column(name = "time_prep")),
            @AttributeOverride(name = "processing", column = @Column(name = "time_processing")),
            @AttributeOverride(name = "clean", column = @Column(name = "time_clean"))
    })
    private Duration time;

    private Float price;

    @Column(name = "price_purchase")
    private Float pricePurchase;

    @ManyToMany(mappedBy = "services")
    @JsonIgnore
    private Set<ServiceCategory> categories;

    public Service() {
    }

    public Service(ServiceRemote remote) {
        externalId = remote.getId();
        update(remote);
    }

    public void update(ServiceRemote remote) {
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
        return this.price == null ? 0 : this.price;
    }

    @Transient
    public Float getPurchasePrice() {
        return this.pricePurchase == null ? 0 : this.pricePurchase;
    }
}
