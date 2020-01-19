package ca.freshstart.data.mails.entity;

import ca.freshstart.types.EntityId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "mail_expressions")
@EqualsAndHashCode(callSuper = true)
public class Expressions extends EntityId {

    @Column(length = 255)
    private String expression;

    private String value;

    private String description;

    public Expressions(){}

    public Expressions(String expression, String value){
        this.expression = expression;
        this.value = value;
    }
}