package ca.freshstart.data.therapist.entity;

import ca.freshstart.data.restriction.entity.Restriction;
import ca.freshstart.helpers.CspDateSerializer;
import ca.freshstart.types.Constants;
import ca.freshstart.types.EntityId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Date;

@Entity
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class TherapistInfo extends EntityId {

    @Column(length = 80, name = "first_name")
    private String firstName;

    @Column(length = 80, name = "last_name")
    private String lastName;

    @Column(length = 80, name = "preferred_name")
    private String preferredName;

    @Column(length = 320, name = "email_personal", unique = true)
    private String emailPersonal;

    @Column(length = 50, name = "phone_mobile")
    private String phoneMobile;

    @Column(length = 50, name = "phone_home")
    private String phoneHome;

    @Column(length = 50, name = "phone_work")
    private String phoneWork;

    @Column(name = "birthday")
    @JsonFormat(pattern = Constants.DATE_FORMAT)
    @Temporal(TemporalType.DATE)
    private Date birthday;

    @Column(length = 100, name = "company")
    private String company;

    @Column(length = 80, name = "street")
    private String street;

    @Column(length = 20, name = "city")
    private String city;

    @Column(length = 50, name = "postcode")
    private String postcode;

    @Column(length = 50, name = "state")
    private String state;

    @Column(length = 80, name = "country")
    private String country;

    @Column(name = "gender")
    private int gender;

    public TherapistInfo() {}

    public TherapistInfo(TherapistInfo data){
        this.firstName = data.getFirstName();
    }
}
