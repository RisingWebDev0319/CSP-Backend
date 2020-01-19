package ca.freshstart.data.suggestions.types;

import ca.freshstart.data.client.entity.Client;
import ca.freshstart.data.service.entity.Service;
import ca.freshstart.data.session.entity.Session;
import ca.freshstart.data.suggestions.entity.SsSessionClientData;
import ca.freshstart.helpers.CspDateSerializer;
import ca.freshstart.types.Constants;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


@Data
@Embeddable
public class PreliminaryEventFK implements Serializable {

    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name = "service_id", nullable = false, updatable = false)
    private Service service;// null for event item

    @Column(name = "date", nullable = false, updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATE_FORMAT)
    @JsonSerialize(using = CspDateSerializer.class)
    private Date date;

    @Embedded
    @ManyToOne
    private SsSessionClientData dataFK;

    public SsSessionClientData getDataFK() {
        if (dataFK == null) this.dataFK = new SsSessionClientData();
        return dataFK;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATE_FORMAT)
    @JsonSerialize(using = CspDateSerializer.class)
    public Date getDate() {
        return date;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATE_FORMAT)
    @JsonSerialize(using = CspDateSerializer.class)
    public void setDate(Date date) {
        this.date = date;
    }

    @Transient
    @JsonIgnore
    public void setDataFK(SsSessionClientData dataFK) {
        getDataFK().setClient(dataFK.getClient());
        getDataFK().setSession(dataFK.getSession());
    }

    @Transient
    public Session getSession() {
        return getDataFK().getSession();
    }

    @Transient
    public Client getClient() {
        return getDataFK().getClient();
    }

    @Transient
    public void setClient(Client client) {
        getDataFK().setClient(client);
    }

    @Transient
    public void setSession(Session session) {
        getDataFK().setSession(session);
    }

    @Transient
    public void setService(Service service) {
        this.service = service;
    }

    @Transient
    public Service getService() {
        return this.service;
    }
}
