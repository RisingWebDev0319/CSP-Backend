package ca.freshstart.data.concreteEvent.entity;

import ca.freshstart.types.EntityId;
import ca.freshstart.types.Constants;
import ca.freshstart.applications.client.helpers.CspTimeDeserializer;
import ca.freshstart.applications.client.helpers.CspTimeSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.sql.Time;

/**
 * ConcreteEvent representation (with entities names instead of ids).
 */
@Entity
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ConcreteEventChangeValue extends EntityId {

    private String event;

    private String service;

    @Column(nullable = false)
    private String room;

    @Column(nullable = false)
    private String therapist;

    @Column(nullable = false)
    private String client;

    @Column(nullable = false)
    private String session;

    private String note;

    @Column(nullable = false)
    @JsonFormat(pattern = Constants.TIME_FORMAT)
    @JsonSerialize(using = CspTimeSerializer.class)
    @JsonDeserialize(using = CspTimeDeserializer.class)
    private Time time;

    @Column(nullable = false)
    private String date;

    private String subStatus;

    private String durationPrep;

    private String durationProcessing;

    private String durationClean;

}
