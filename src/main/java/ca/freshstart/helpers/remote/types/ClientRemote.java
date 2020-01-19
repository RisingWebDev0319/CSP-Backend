package ca.freshstart.helpers.remote.types;

import ca.freshstart.types.IdIf;
import lombok.Data;

@Data
public class ClientRemote implements IdIf<Long> {
    private Long id;
    private String name;
    private String recordtype;
    private ClientColumnsRemote columns;

    @Data
    public static class ClientColumnsRemote {
        private String entityid;
        private String altname;
    }
}