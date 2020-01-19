package ca.freshstart.applications.initialization;

import ca.freshstart.data.suggestions.entity.SsCustomColumn;
import ca.freshstart.data.suggestions.entity.SsCustomColumnValue;
import ca.freshstart.data.suggestions.entity.SsTableColumn;
import ca.freshstart.data.suggestions.repository.SsCustomColumnRepository;
import ca.freshstart.data.suggestions.repository.SsTableColumnRepository;
import ca.freshstart.data.suggestions.types.SuggestedServicesColumnType;
import ca.freshstart.data.suggestions.types.SuggestedServicesCustomColumnType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MockSuggestions {
    private static final Logger logger = LoggerFactory.getLogger(MockData.class);

    private final SsCustomColumnRepository ssCustomColumnRepository;
    private final SsTableColumnRepository ssTableColumnRepository;


    public void mock() {
        logger.info("-- [ Start mocking suggestions data ] --");

        mockTableColumns();

        logger.info("-- [ End mocking suggestions data ] --");
    }

    private void mockTableColumns() {
        SsTableColumn tableColumn1 = new SsTableColumn();
        tableColumn1.setTitle("Column Flag");
        tableColumn1.setType(SuggestedServicesColumnType.flag);

        ssTableColumnRepository.save(tableColumn1);

        SsTableColumn tableColumn2 = new SsTableColumn();
        tableColumn2.setTitle("Column Name");
        tableColumn2.setType(SuggestedServicesColumnType.name);

        ssTableColumnRepository.save(tableColumn2);

        SsTableColumn tableColumn3 = new SsTableColumn();
        tableColumn3.setTitle("Column Status");
        tableColumn3.setType(SuggestedServicesColumnType.status);

        ssTableColumnRepository.save(tableColumn3);

        SsCustomColumn customColumn = new SsCustomColumn();
        customColumn.setType(SuggestedServicesCustomColumnType.checkbox);
        customColumn.setTitle("Custom column checkbox");

        SsCustomColumnValue value = new SsCustomColumnValue();
        value.setTitle("Value 1");
        value.setValue(123L);
        value.setOrder(1L);

        customColumn.addValue(value);

        ssCustomColumnRepository.save(customColumn);

        SsTableColumn tableColumn4 = new SsTableColumn();
        tableColumn4.setTitle("Column Custom");
        tableColumn4.setType(SuggestedServicesColumnType.custom);
        tableColumn4.setCustomColumn(customColumn);

        ssTableColumnRepository.save(tableColumn4);

        SsCustomColumn customColumn2 = new SsCustomColumn();
        customColumn2.setType(SuggestedServicesCustomColumnType.input);
        customColumn2.setTitle("Custom column input");

        SsCustomColumnValue value2 = new SsCustomColumnValue();
        value2.setTitle("Value 2");
        value2.setValue(124L);
        value2.setOrder(2L);

        customColumn2.addValue(value2);

        ssCustomColumnRepository.save(customColumn2);

        logger.info("-- Mocked Table columns --");
    }

}
