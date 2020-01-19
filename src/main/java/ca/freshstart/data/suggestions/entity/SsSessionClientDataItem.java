package ca.freshstart.data.suggestions.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@AssociationOverrides({
        @AssociationOverride(name = "id.columnId",
                joinColumns = @JoinColumn(name = "column_id")),
        @AssociationOverride(name = "id.dataFK",
                joinColumns = {
                        @JoinColumn(name = "client_id",
                                referencedColumnName = "client_id"),
                        @JoinColumn(name = "session_id",
                                referencedColumnName = "session_id")})})
@Data
@ToString
public class SsSessionClientDataItem {
    @Embeddable
    @Data
    @ToString(exclude = "dataFK")
    public static class PK implements Serializable {

        @Column(name = "column_id", nullable = false, updatable = false)
        private Long columnId;

        @ManyToOne
        SsSessionClientData dataFK;

        public SsSessionClientData getDataFK() {
            if (dataFK == null) dataFK = new SsSessionClientData();
            return dataFK;
        }
    }

    @EmbeddedId
    @JsonIgnore
    private PK id;

    private String textValue;

    private Long selectValue;

    @Transient
    public long getColumnId() {
        return getId().getColumnId();
    }

    @Transient
    public void setColumnId(Long columnId) {
        getId().setColumnId(columnId);
    }

    @Transient
    @JsonIgnore
    public SsSessionClientData getDataFK() {
        return getId().getDataFK();
    }

    @Transient
    @JsonIgnore
    public void setDataFK(SsSessionClientData dataFK) {
        getId().setDataFK(dataFK);
    }

    public PK getId() {
        if (id == null) id = new PK();
        return id;
    }
}
