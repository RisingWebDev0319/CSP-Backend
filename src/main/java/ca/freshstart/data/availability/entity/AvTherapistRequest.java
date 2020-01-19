package ca.freshstart.data.availability.entity;

import ca.freshstart.types.EntityId;
import ca.freshstart.data.availability.types.AvTherapistRequestStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Data
@ToString(exclude = {"avRequest"})
@EqualsAndHashCode(exclude = {"avRequest"}, callSuper = true)
public class AvTherapistRequest extends EntityId {

    @Column(name = "therapist_id", nullable = false)
    private Long therapistId;

    private String message;

    @Enumerated(EnumType.STRING)
    private AvTherapistRequestStatus status = AvTherapistRequestStatus.created;

    @ManyToOne
    @JoinColumn(name = "av_request_id")
    @JsonIgnore
    private AvRequest avRequest;
}