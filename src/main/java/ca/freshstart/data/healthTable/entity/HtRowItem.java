package ca.freshstart.data.healthTable.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;

@ToString
@Entity
@Data
@AssociationOverrides({
        @AssociationOverride(name = "id.columnId",
                joinColumns = @JoinColumn(name = "column_id")),
        @AssociationOverride(name = "id.dataFK",
                joinColumns = {
                        @JoinColumn(name = "client_id",
                                referencedColumnName = "client_id"),
                        @JoinColumn(name = "session_id",
                                referencedColumnName = "session_id")})})
public class HtRowItem {

    @Embeddable
    @Data
    @ToString(exclude = "dataFK")
    public static class PK implements Serializable {

        @Column(name = "column_id", nullable = false, updatable = false)
        private long columnId;

        @ManyToOne
        HtRow dataFK;

        public HtRow getDataFK() {
            if (dataFK == null) dataFK = new HtRow();
            return dataFK;
        }
    }

    @EmbeddedId
    @JsonIgnore
    private PK id;

    private String textValue;

    private int selectValue;

    @Transient
    public long getColumnId() {
        return getId().getColumnId();
    }

    @Transient
    public void setColumnId(long columnId) {
        getId().setColumnId(columnId);
    }

    @Transient
    @JsonIgnore
    public HtRow getDataFK() {
        return getId().getDataFK();
    }

    @Transient
    @JsonIgnore
    public void setDataFK(HtRow dataFK) {
        getId().setDataFK(dataFK);
    }


    /**
     * accessors
     */
    public PK getId() {
        if (id == null) id = new PK();
        return id;
    }

}
