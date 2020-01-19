package ca.freshstart.data.appUser.entity;

import ca.freshstart.types.EntityId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class AppUser extends EntityId {

    @Column(length = 255)
    private String name;

//    @JsonIgnore
    private String password; // transient

    @Column(unique = true)
    private String email;

    private Boolean locked = false;

    @ElementCollection//(targetClass = Module.class)
    @CollectionTable(name="user_modules", joinColumns=@JoinColumn(name="user_id"))
    @Column(name="module", nullable = false)
//    @Enumerated(EnumType.STRING)
    private List<String> modules;

    public AppUser() {}

    public AppUser(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public AppUser(String name, String password, List<String> modules) {
        this.name = name;
        this.password = password;
        this.modules = modules;
    }
}