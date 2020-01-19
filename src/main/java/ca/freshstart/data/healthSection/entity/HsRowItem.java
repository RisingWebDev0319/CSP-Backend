package ca.freshstart.data.healthSection.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@AssociationOverrides({
        @AssociationOverride(name = "id.columnId",
                joinColumns = @JoinColumn(name = "column_id")),
        @AssociationOverride(name = "id.row",
                joinColumns = @JoinColumn(name = "row_id", referencedColumnName = "id"))
})
@Data
public class HsRowItem {

    @Embeddable
    @Data
    @ToString(exclude = "row")
    public static class PK implements Serializable {

        @Column(name = "column_id", nullable = false, updatable = false)
        private long columnId;

        @ManyToOne
        @JsonIgnore
        private HsRow row;

    }

    @EmbeddedId
    private PK id;

    @Column(columnDefinition = "TEXT")
    private String textValue;

    private long selectValue;

    private String colorValue;

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
    public HsRow getRow() {
        return getId().getRow();
    }

    @Transient
    @JsonIgnore
    public void setRow(HsRow row) {
        getId().setRow(row);
    }


    /**
     * accessors
     */
    public PK getId() {
        if (id == null) id = new PK();
        return id;
    }

}
