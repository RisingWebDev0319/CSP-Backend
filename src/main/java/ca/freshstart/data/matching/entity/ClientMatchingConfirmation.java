package ca.freshstart.data.matching.entity;

import ca.freshstart.data.client.entity.Client;
import ca.freshstart.data.session.entity.Session;
import ca.freshstart.data.suggestions.entity.PreliminaryEvent;
import ca.freshstart.data.matching.types.PreliminaryEventType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@ToString
public class ClientMatchingConfirmation {

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

    private String secret; // key to get access to confirmation info without login

    @OneToMany(mappedBy = "matchingConfirmation", fetch = FetchType.EAGER, cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<PreliminaryEvent> items = new ArrayList<>();

    public void setItems(List<PreliminaryEvent> items) {
        removeAllItems();

        if (items != null) {
            items.forEach(this::addItem);
        }
    }

    public void addItem(PreliminaryEvent item) {
        item.setMatchingConfirmation(this);
        item.setState(PreliminaryEventType.confirmation);
        items.add(item);
    }

    public List<PreliminaryEvent> removeAllItems() {
        this.items.forEach(item -> {
            item.setMatchingConfirmation(null);
            item.setState(null);
        });
        List<PreliminaryEvent> returned = new ArrayList<>(this.items);
        this.items.clear();
        return returned;
    }

    @Transient
    public Client getClient() {
        return getId().getClient();
    }

    @Transient
    public Long getClientId() {
        return getClient().getId();
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
