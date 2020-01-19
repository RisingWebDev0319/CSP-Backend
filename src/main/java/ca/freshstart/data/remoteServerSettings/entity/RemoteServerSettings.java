package ca.freshstart.data.remoteServerSettings.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class RemoteServerSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private long updateTime;
    private String cronExpression;
    private String cronTimezone;

    @Embedded
    @AttributeOverrides( {
            @AttributeOverride(name="clients", column = @Column(name="clients_url") ),
            @AttributeOverride(name="clientInfo", column = @Column(name="client_info_url") ),
            @AttributeOverride(name="therapists", column = @Column(name="therapists_url") ),
            @AttributeOverride(name="services", column = @Column(name="services_url") ),
            @AttributeOverride(name="events", column = @Column(name="events_url") ),
            @AttributeOverride(name="protocols", column = @Column(name="protocols_url") ),
            @AttributeOverride(name="protocolPackages", column = @Column(name="protocol_packages_url") ),
            @AttributeOverride(name="estimates", column = @Column(name="estimates_url") )
    } )
    private RemoteUrls urls;

    @Embeddable
    @Data
    public static class RemoteUrls {
        private String clients;
        private String clientInfo;
        private String therapists;
        private String services;
        private String events;
        private String protocols;
        private String protocolPackages;
        private String estimates;

        public RemoteUrls() {}
    }
}