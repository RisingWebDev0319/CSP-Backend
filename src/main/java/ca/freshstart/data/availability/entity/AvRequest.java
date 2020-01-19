package ca.freshstart.data.availability.entity;

import ca.freshstart.types.EntityId;
import ca.freshstart.types.Constants;
import ca.freshstart.helpers.CspDateSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class AvRequest extends EntityId {

    @Column(nullable = false)
    @JsonFormat(pattern = Constants.DATE_FORMAT)
    @JsonSerialize(using = CspDateSerializer.class)
    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Column(nullable = false)
    @JsonFormat(pattern = Constants.DATE_FORMAT)
    @JsonSerialize(using = CspDateSerializer.class)
    @Temporal(TemporalType.DATE)
    private Date endDate;

    @JsonIgnore
    private boolean archived = false;

    @OneToMany(mappedBy = "avRequest", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AvTherapistRequest> therapistsRequests = new ArrayList<>();

    public void addRequest(AvTherapistRequest request) {
        request.setAvRequest(this);
        therapistsRequests.add(request);
    }
}