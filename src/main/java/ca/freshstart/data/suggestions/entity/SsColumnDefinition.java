package ca.freshstart.data.suggestions.entity;

import ca.freshstart.data.suggestions.types.SuggestedServicesColumnType;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SsColumnDefinition {
    private Long id;
    private String title;
    private SuggestedServicesColumnType type;
    private Long customColumnId;
    private Long position;
}
