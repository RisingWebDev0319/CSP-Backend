package ca.freshstart.data.healthTable.types;

import lombok.Data;

@Data
public class HtColumnDefinition {
    private Long id;
    private String title;
    private String type;
    private Integer position;

    private Long customColumnId;
}
