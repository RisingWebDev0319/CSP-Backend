package ca.freshstart.data.reconcile.entity;

import ca.freshstart.data.appUser.entity.AppUser;
import ca.freshstart.types.EntityId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Audit extends EntityId {

    @ManyToOne
    @JoinColumn(name="user_id")
    @JsonIgnore
    private AppUser user;

    @Column(nullable = false)
    private String userSignature;

    public String getUserName() {
        return user.getName();
    }

    @OneToMany(mappedBy = "audit", fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    private List<ConcreteEventReconcile> concreteEventsReconcile = new ArrayList<>();

}
