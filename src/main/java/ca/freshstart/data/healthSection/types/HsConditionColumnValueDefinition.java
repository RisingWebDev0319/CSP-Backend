package ca.freshstart.data.healthSection.types;

import lombok.Data;

import java.util.Set;

@Data
public class HsConditionColumnValueDefinition {
    private Long id;
    private String title;
    private String defaultFlagColor;
    private Set<HsTableType> sections;
}
