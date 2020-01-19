package ca.freshstart.data.taxes.entity;

import ca.freshstart.types.EntityId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Taxes extends EntityId {

    @Column(unique = true)
    private String code;

    private String title;

    @JsonIgnore
    private boolean archived = false;
}
