package ca.freshstart.data.mails.entity;

import ca.freshstart.data.equipment.entity.Equipment;
import ca.freshstart.types.EntityId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.ManyToMany;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import javax.persistence.FetchType;

import java.util.Set;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class Mails extends EntityId {

    @OneToOne
    @JoinColumn(name="id")
    private MailTypes type;

    private String body;

    @Column(length = 255)
    private String subject;

    private boolean active;

    @ManyToMany(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @JoinTable(name="mails_to_expressions",
            joinColumns={@JoinColumn(name="mail_id")},
            inverseJoinColumns={@JoinColumn(name="expression_id")})
    private Set<Expressions> expressions;

    public boolean getActive(){
        return this.active;
    }
}
