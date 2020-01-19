package ca.freshstart.data.healthTable.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@EqualsAndHashCode(exclude = {"values"})
public class HtRow {

    @Embeddable
    @Data
    public static class PK implements Serializable {

        @Column(name = "client_id", nullable = false, updatable = false)
        private long clientId;

        @Column(name = "session_id", nullable = false, updatable = false)
        private long sessionId;

    }


    @EmbeddedId
    @JsonIgnore
    private PK id;

    @OneToMany(mappedBy = "id.dataFK", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<HtRowItem> values = new HashSet<>();

    public void addValue(HtRowItem value){
        values.add(value);
        value.setDataFK(this);
    }

    public void removeValue(HtRowItem value){
        value.setDataFK(null);
        values.remove(value);
    }


    @Transient
    public long getClientId() {
        return getId().getClientId();
    }

    @Transient
    public void setClientId(long clientId) {
        getId().setClientId(clientId);
    }

    @Transient
    public long getSessionId() {
        return getId().getSessionId();
    }

    @Transient
    public void setSessionId(long sessionId) {
        getId().setSessionId(sessionId);
    }


    /**
     * accessors
     */
    public PK getId() {
        if (id == null) id = new PK();
        return id;
    }

}
