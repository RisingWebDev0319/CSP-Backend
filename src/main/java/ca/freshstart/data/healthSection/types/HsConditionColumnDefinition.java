package ca.freshstart.data.healthSection.types;

import lombok.Data;

import java.util.List;

@Data
public class HsConditionColumnDefinition {
    private Long id;
    private String sectionType;
    private List<HsConditionColumnValueDefinition> values;
}
