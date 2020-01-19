package ca.freshstart.data.protocol.entity;

import ca.freshstart.types.ExternalIdIf;
import ca.freshstart.types.Updateable;
import ca.freshstart.types.EntityId;
import ca.freshstart.helpers.remote.types.ProtocolRemote;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class Protocol extends EntityId implements ExternalIdIf<Long>, Updateable<ProtocolRemote> {
    private Long externalId;
    private String name;
    private boolean packageSign;
    private String price;

    public Protocol() {
    }

    public Protocol(ProtocolRemote remote) {
        externalId = remote.getId();
        update(remote);
    }

    public void update(ProtocolRemote remote) {
        if(remote == null) {
            return;
        }

        setName(remote.getName());
        setPackageSign(remote.getIs_package() == 1);
        setPrice(remote.getPrice());
    }

}
