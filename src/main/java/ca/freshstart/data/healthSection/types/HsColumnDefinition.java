package ca.freshstart.data.healthSection.types;

import lombok.Data;

@Data
public class HsColumnDefinition {
    private Long id;
    private String title;
    private String type;
    private Integer position;

    private Long customColumnId;
    private Long selectColumnId;
    private Long flagColumnId;
    private Long conditionColumnId;
}
