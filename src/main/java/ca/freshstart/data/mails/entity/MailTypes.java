package ca.freshstart.data.mails.entity;

import ca.freshstart.types.EntityId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class MailTypes extends EntityId {

    @Column(length = 20)
    private String key;

    @Column(length = 255)
    private String title;

    private Long order;
}
