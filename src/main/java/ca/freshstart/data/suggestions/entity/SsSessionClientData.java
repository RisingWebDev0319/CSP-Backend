package ca.freshstart.data.suggestions.entity;

import ca.freshstart.data.client.entity.Client;
import ca.freshstart.data.session.entity.Session;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@ToString
@EqualsAndHashCode(exclude = {"values", "services"})
@AssociationOverrides({
        @AssociationOverride(name = "client",
                joinColumns = @JoinColumn(name = "client_id")),
        @AssociationOverride(name = "session",
                joinColumns = @JoinColumn(name = "session_id"))})
public class SsSessionClientData {

    @Embeddable
    @Data
    public static class PK implements Serializable {

        @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
        @JoinColumn(name = "client_id", nullable = false, updatable = false)
        private Client client;

        @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
        @JoinColumn(name = "session_id", nullable = false, updatable = false)
        private Session session;
    }

    @EmbeddedId
    @JsonIgnore
    private PK id;

    @OneToMany(mappedBy = "id.dataFK", fetch = FetchType.EAGER, cascade = CascadeType.MERGE, orphanRemoval = true)
    private Set<SsSessionClientDataItem> values = new HashSet<>();

    @OneToMany(mappedBy = "fk.dataFK", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PreliminaryEvent> services = new HashSet<>();

    public void addValue(SsSessionClientDataItem value) {
        values.add(value);
        value.setDataFK(this);
    }

    public void removeValue(SsSessionClientDataItem value) {
        value.setDataFK(null);
        values.remove(value);
    }

    public void addService(PreliminaryEvent value) {
        services.add(value);
        value.setDataFK(this);
    }

    public void removeService(PreliminaryEvent value) {
        services.remove(value);
    }

    @Transient
    public Client getClient() {
        return getId().getClient();
    }

    @Transient
    public void setClient(Client client) {
        getId().setClient(client);
    }

    @Transient
    public Session getSession() {
        return getId().getSession();
    }

    @Transient
    public void setSession(Session session) {
        getId().setSession(session);
    }

    public PK getId() {
        if (id == null) id = new PK();
        return id;
    }
}
