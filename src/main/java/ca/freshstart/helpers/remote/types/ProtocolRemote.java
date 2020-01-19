package ca.freshstart.helpers.remote.types;

import ca.freshstart.types.IdIf;
import lombok.Data;

@Data
public class ProtocolRemote implements IdIf<Long> {
    private Long id;
    private String name;
    private int is_package;
    private String price;
}