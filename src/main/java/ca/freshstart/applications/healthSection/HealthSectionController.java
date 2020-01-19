package ca.freshstart.applications.healthSection;

import ca.freshstart.data.healthSection.entity.HsColumn;
import ca.freshstart.data.healthSection.entity.*;
import ca.freshstart.data.healthSection.repository.*;
import ca.freshstart.data.protocol.entity.Protocol;
import ca.freshstart.exceptions.BadRequestException;
import ca.freshstart.exceptions.NotFoundException;
import ca.freshstart.types.IdResponse;
import ca.freshstart.data.healthSection.types.*;
import ca.freshstart.data.protocol.repository.ProtocolRepository;
import ca.freshstart.helpers.CspUtils;
import ca.freshstart.helpers.CspUtils.CollectionDiffResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class HealthSectionController {

    private final HsColumnRepository hsColumnRepository;
    private final HsTableRepository hsTableRepository;
    private final HsCustomColumnRepository hsCustomColumnRepository;
    private final HsSelectColumnRepository hsSelectColumnRepository;
    private final HsFlagColumnRepository hsFlagColumnRepository;
    private final HsConditionColumnRepository hsConditionColumnRepository;
    private final HsConditionColumnValueRepository hsConditionColumnValueRepository;
    private final HsRowRepository hsRowRepository;
    private final HsRowItemRepository hsRowItemRepository;
    private final ProtocolRepository protocolRepository;


    @RequestMapping(value = "/healthSections", method = RequestMethod.GET)
    @Secured("ROLE_HEALTH")
    public Collection<HsTable> getHsTables() {

        return hsTableRepository.findAll();
    }

    @RequestMapping(value = "/healthSection/{sectionType}/columns", method = RequestMethod.GET)
    @Secured("ROLE_HEALTH")
    public Collection<HsColumn> getSectionColumns(@PathVariable("sectionType") String sectionType) {

        return hsColumnRepository.findPositionedBySectionType(HsTableType.valueOf(sectionType));
    }

    @RequestMapping(value = "/healthSection/{sectionType}/columns", method = RequestMethod.PUT)
    @Secured("ROLE_HEALTH_UI")
    public void putSectionColumns(@PathVariable("sectionType") String sectionType,
                                  @RequestBody HsColumnDefinition[] columnDefinitions) {

        HsTableType sectionType1 = HsTableType.valueOf(sectionType);

        for (HsColumnDefinition definition : columnDefinitions) {

            HsColumn column = hsColumnRepository.findBySectionTypeAndId(sectionType1, definition.getId())
                    .orElseThrow(() -> new NotFoundException("No such HsColumn"));

            column = fillColumnFromDefinition(column, definition, sectionType1);

            hsColumnRepository.save(column);
        }
    }

    @RequestMapping(value = "/healthSection/{sectionType}/column", method = RequestMethod.POST)
    @Secured("ROLE_HEALTH_UI")
    public IdResponse postSectionColumn(@PathVariable("sectionType") String sectionType,
                                        @RequestBody HsColumnDefinition request) {

        HsTableType sectionType1 = HsTableType.valueOf(sectionType);
        HsColumnType type = HsColumnType.valueOf(request.getType());

        switch (type) {
            case cost:
            case flag:
            case condition: {
                List<HsColumn> sameTypeColumns = hsColumnRepository.findPositionedBySectionTypeAndTypeAndNotPermanent(sectionType1, type);
                if (sameTypeColumns.size() > 0) {
                    throw new BadRequestException("Can't add more than one standard field");
                }
            }
            default:
                break;
        }

        List<HsColumn> hidden;
        if (type == HsColumnType.custom) {
            Long customColumnId = request.getCustomColumnId();
            hidden = hsColumnRepository.findNotPositionedBySectionTypeAndTypeAndCustomColumnId(sectionType1, type, customColumnId);
        } else {
            hidden = hsColumnRepository.findNotPositionedBySectionTypeAndType(sectionType1, type);
        }

        HsColumn column;
        if (hidden.isEmpty()) {
            column = new HsColumn();
        } else {
            column = hidden.get(0);
        }

        column = fillColumnFromDefinition(column, request, sectionType1);

        HsColumn savedColumn = hsColumnRepository.save(column);

        HsTable hsTable = hsTableRepository.findBySectionType(HsTableType.valueOf(sectionType))
                .orElseThrow(() -> new NotFoundException("No such HsTable"));

        hsTable.addColumn(savedColumn);
        hsTableRepository.save(hsTable);

        return new IdResponse(savedColumn.getId());
    }

    @RequestMapping(value = "/healthSection/{sectionType}/column/{columnId}", method = RequestMethod.GET)
    @Secured("ROLE_HEALTH")
    public HsColumn getSectionColumn(@PathVariable("sectionType") String sectionType,
                                     @PathVariable("columnId") Long columnId) {

        return hsColumnRepository.findBySectionTypeAndId(HsTableType.valueOf(sectionType), columnId)
                .orElseThrow(() -> new NotFoundException("No such HsColumn"));
    }


    @RequestMapping(value = "/healthSection/{sectionType}/column/{columnId}", method = RequestMethod.PUT)
    @Secured("ROLE_HEALTH_UI")
    public void putSectionColumn(@PathVariable("sectionType") String sectionType,
                                 @PathVariable("columnId") Long columnId,
                                 @RequestBody HsColumnDefinition columnDefinition) {

        HsTableType sectionType1 = HsTableType.valueOf(sectionType);

        HsColumn column = hsColumnRepository.findBySectionTypeAndId(sectionType1, columnId)
                .orElseThrow(() -> new NotFoundException("No such HsColumn"));

        column = fillColumnFromDefinition(column, columnDefinition, sectionType1);

        hsColumnRepository.save(column);
    }


    @RequestMapping(value = "/healthSection/{sectionType}/column/{columnId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK, reason = "Deleted")
    @Secured("ROLE_HEALTH_UI")
    public void deleteColumn(@PathVariable("sectionType") String sectionType,
                             @PathVariable("columnId") Long columnId) {

        HsColumn column = hsColumnRepository.findBySectionTypeAndId(HsTableType.valueOf(sectionType), columnId)
                .orElseThrow(() -> new NotFoundException("No such HsColumn"));

        if (column.isPermanent())
            throw new BadRequestException("Permanent column can't be deleted or hidden");

        column.setPosition(null);
        hsColumnRepository.save(column);
    }


    @RequestMapping(value = "/healthSection/{sectionType}/column/{columnId}/values", method = RequestMethod.PUT)
    @Secured("ROLE_HEALTH_UI")
    public void putSectionColumnVaues(@PathVariable("sectionType") String sectionType,
                                      @PathVariable("columnId") Long columnId,
                                      @RequestBody HsSelectColumnValue[] values) {

        HsTableType sectionType1 = HsTableType.valueOf(sectionType);
        HsColumn column = hsColumnRepository.findBySectionTypeAndId(sectionType1, columnId)
                .orElseThrow(() -> new NotFoundException("No such HsColumn"));

        HsSelectColumn selectColumn = column.getSelectColumn();

        if (selectColumn == null) {
            selectColumn = new HsSelectColumn();
            selectColumn.setSectionType(sectionType1);
            selectColumn = hsSelectColumnRepository.save(selectColumn);
            column.setSelectColumn(selectColumn);
        }

        CspUtils.mergeValues(
                selectColumn.getValues(),
                Arrays.asList(values),
                selectColumn::removeValue,
                selectColumn::addValue,
                (to, from) -> {
                    to.setTitle(from.getTitle());
                    to.setValue(from.getValue());
                    to.setPosition(from.getPosition());
                });

        hsColumnRepository.save(column);
    }

    private HsColumn fillColumnFromDefinition(HsColumn column, HsColumnDefinition definition, HsTableType sectionType) {
        HsColumnType type = HsColumnType.valueOf(definition.getType());
        column.setType(type);
        column.setTitle(definition.getTitle());
        column.setPosition(definition.getPosition());

        switch (type) {
            case custom:
                Long customColumnId = definition.getCustomColumnId();
                if (customColumnId != null) {
                    HsCustomColumn customColumn = hsCustomColumnRepository.findById(customColumnId)
                            .orElseThrow(() -> new NotFoundException("No such HsCustomColumn"));
                    column.setCustomColumn(customColumn);
                }
                break;
            case select:
                Long selectColumnId = definition.getSelectColumnId();
                if (selectColumnId != null) {
                    HsSelectColumn selectColumn = hsSelectColumnRepository.findById(selectColumnId)
                            .orElseThrow(() -> new NotFoundException("No such HsCustomColumn"));
                    column.setSelectColumn(selectColumn);
                }
                break;
            case flag:
            case condition:
            default:
                break;
        }

        return column;
    }

    @RequestMapping(value = "/healthSection/{sectionType}/customColumns", method = RequestMethod.GET)
    @Secured("ROLE_HEALTH_UI")
    public Collection<HsCustomColumn> getHsCustomColumns(@PathVariable("sectionType") String sectionType) {

        return hsCustomColumnRepository.findBySectionType(HsTableType.valueOf(sectionType));
    }

    @RequestMapping(value = "/healthSection/{sectionType}/customColumn", method = RequestMethod.POST)
    @Secured("ROLE_HEALTH_UI")
    public IdResponse postCustomColumn(@PathVariable("sectionType") String sectionType,
                                       @RequestBody HsCustomColumn customColumn) {
        customColumn.setSectionType(HsTableType.valueOf(sectionType));
        HsCustomColumn savedCustomColumn = hsCustomColumnRepository.save(customColumn);
        return new IdResponse(savedCustomColumn.getId());
    }

    @RequestMapping(value = "/healthSection/{sectionType}/customColumn/{columnId}", method = RequestMethod.GET)
    @Secured("ROLE_HEALTH_UI")
    public HsCustomColumn getCustomColumn(@PathVariable("sectionType") String sectionType,
                                          @PathVariable("columnId") Long columnId) {

        return hsCustomColumnRepository.findBySectionTypeAndId(HsTableType.valueOf(sectionType), columnId)
                .orElseThrow(() -> new NotFoundException("No such HtCustomColumn"));
    }

    @RequestMapping(value = "/healthSection/{sectionType}/customColumn/{columnId}", method = RequestMethod.PUT)
    @Secured("ROLE_HEALTH_UI")
    public void putCustomColumn(@PathVariable("sectionType") String sectionType,
                                @PathVariable("columnId") Long columnId,
                                @RequestBody HsCustomColumn customColumn) {

        long id = customColumn.getId();
        if (id == columnId) {
            customColumn.setSectionType(HsTableType.valueOf(sectionType));
            hsCustomColumnRepository.save(customColumn);
        } else {
            throw new BadRequestException("HsCustomColumn.id does not match columnId in path parameter");
        }
    }

    @RequestMapping(value = "/healthSection/{sectionType}/customColumn/{columnId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK, reason = "Deleted")
    @Secured("ROLE_HEALTH_UI")
    public void deleteCustomColumn(@PathVariable("sectionType") String sectionType,
                                   @PathVariable("columnId") Long columnId) {

        HsCustomColumn customColumn = hsCustomColumnRepository.findBySectionTypeAndId(HsTableType.valueOf(sectionType), columnId)
                .orElseThrow(() -> new NotFoundException("No such HtCustomColumn"));
        hsCustomColumnRepository.delete(customColumn);
    }


    @RequestMapping(value = "/healthSection/{sectionType}/customColumn/{columnId}/values", method = RequestMethod.PUT)
    @Secured("ROLE_HEALTH_UI")
    public void putCustomColumnValues(@PathVariable("sectionType") String sectionType,
                                      @PathVariable("columnId") Long columnId,
                                      @RequestBody HsCustomColumnValue[] values) {

        HsCustomColumn customColumn = hsCustomColumnRepository.findBySectionTypeAndId(HsTableType.valueOf(sectionType), columnId)
                .orElseThrow(() -> new NotFoundException("No such HtCustomColumn"));

        CspUtils.mergeValues(
                customColumn.getValues(),
                Arrays.asList(values),
                customColumn::removeValue,
                customColumn::addValue,
                (to, from) -> {
                    to.setTitle(from.getTitle());
                    to.setValue(from.getValue());
                    to.setPosition(from.getPosition());
                });

        hsCustomColumnRepository.save(customColumn);
    }

    @RequestMapping(value = "/healthFlags", method = RequestMethod.GET)
    @Secured({"ROLE_HEALTH", "ROLE_HEALTH_UI"})
    public List<HsFlagColumn> getHsFlagColumns() {

        List<HsFlagColumn> all = hsFlagColumnRepository.findAll();
        if (all.isEmpty())
            throw new NotFoundException("No one HsFlagColumn");
        return all;
    }

    @RequestMapping(value = "/healthFlag/{sectionType}", method = RequestMethod.GET)
    @Secured("ROLE_HEALTH")
    public HsFlagColumn getHsFlagColumn(@PathVariable("sectionType") String sectionType) {

        List<HsFlagColumn> all = hsFlagColumnRepository.findBySectionType(HsTableType.valueOf(sectionType));
        if (all.isEmpty())
            throw new NotFoundException("No one HsFlagColumn");
        return all.get(0);
    }

    @RequestMapping(value = "/healthFlag/{sectionType}", method = RequestMethod.PUT)
    @Secured("ROLE_HEALTH_UI")
    public void putHsFlagColumn(@PathVariable("sectionType") String sectionType,
                                @RequestBody HsFlagColumn request) {

        List<HsFlagColumn> all = hsFlagColumnRepository.findBySectionType(HsTableType.valueOf(sectionType));
        if (all.isEmpty())
            throw new NotFoundException("No one HsFlagColumn");
        HsFlagColumn flagColumn = all.get(0);

        CspUtils.mergeValues(
                flagColumn.getValues(),
                request.getValues(),
                flagColumn::removeValue,
                flagColumn::addValue,
                (to, from) -> {
                    to.setTitle(from.getTitle());
                    to.setColor(from.getColor());
                });

        hsFlagColumnRepository.save(flagColumn);
    }

    @RequestMapping(value = "/healthConditions", method = RequestMethod.GET)
    @Secured({"ROLE_HEALTH", "ROLE_HEALTH_UI"})
    public List<HsConditionColumn> getHsConditionColumns() {

        List<HsConditionColumn> all = hsConditionColumnRepository.findAll();
        if (all.isEmpty())
            throw new NotFoundException("No one HsConditionColumn");
        return all;
    }

    @RequestMapping(value = "/healthCondition/{sectionType}", method = RequestMethod.GET)
    @Secured("ROLE_HEALTH")
    public HsConditionColumn getHsConditionColumn(@PathVariable("sectionType") String sectionType) {

        List<HsConditionColumn> all = hsConditionColumnRepository.findBySectionType(HsTableType.valueOf(sectionType));
        if (all.isEmpty())
            throw new NotFoundException("No one HsConditionColumn");
        return all.get(0);
    }

    @RequestMapping(value = "/healthCondition/{sectionType}", method = RequestMethod.PUT)
    @Secured("ROLE_HEALTH_UI")
    @Transactional
    public void putHsConditionColumn(@PathVariable("sectionType") String sectionType,
                                     @RequestBody HsConditionColumnDefinition request) {

        List<HsConditionColumn> all = hsConditionColumnRepository.findBySectionType(HsTableType.valueOf(sectionType));
        if (all.isEmpty())
            throw new NotFoundException("No one HsFlagColumn");
        HsConditionColumn conditionColumn = all.get(0);

        Set<HsConditionColumnValue> values = conditionColumn.getValues();

        CollectionDiffResult<HsConditionColumnValue, HsConditionColumnValueDefinition> diff = CspUtils.diffCollections(
                values, request.getValues(),
                (value, valueDefinition) -> value.getId() != null
                        && valueDefinition.getId() != null
                        && value.getId().equals(valueDefinition.getId())
        );

        // remove unused
        List<HsConditionColumnValue> valuesToDelete = diff.getRemoved();

        if (!valuesToDelete.isEmpty()) {
            valuesToDelete.forEach(value -> {
                value.getConditionColumns().forEach(column -> {
                    column.removeValue(value);
                    hsConditionColumnRepository.save(column);
                });
                value.getConditionColumns().clear();
                hsConditionColumnValueRepository.delete(value);
            });
        }

        // create new to add
        List<HsConditionColumnValue> valuesToAdd = diff.getAdded().stream().map(valueDefinitionToAdd -> {
            List<HsConditionColumn> columnsToLink = hsConditionColumnRepository.findBySectionTypes(valueDefinitionToAdd.getSections());
            if (columnsToLink.isEmpty()) {
                return null;
            }
            HsConditionColumnValue newValue = new HsConditionColumnValue();
            newValue.setTitle(valueDefinitionToAdd.getTitle());
            newValue.setDefaultFlagColor(valueDefinitionToAdd.getDefaultFlagColor());
            columnsToLink.forEach(newValue::addConditionColumn);
            return newValue;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        if (!valuesToAdd.isEmpty()) {
            hsConditionColumnValueRepository.save(valuesToAdd);
        }

        // edit
        List<HsConditionColumnValue> valuesToEdit = diff.getNotMoved();

        if (!valuesToEdit.isEmpty()) {
            Map<Long, HsConditionColumnValueDefinition> id2ValueDefinition = new HashMap<>();
            request.getValues().forEach(valueDefinition -> {
                if (valueDefinition.getId() != null) {
                    id2ValueDefinition.put(valueDefinition.getId(), valueDefinition);
                }
            });

            valuesToEdit.forEach(valueToEdit ->
                    editHsConditionColumnValue(valueToEdit, id2ValueDefinition.get(valueToEdit.getId())));
        }

    }

    private void editHsConditionColumnValue(HsConditionColumnValue valueToEdit,
                                            HsConditionColumnValueDefinition valueDefinitionToEdit) {
        valueToEdit.setTitle(valueDefinitionToEdit.getTitle());
        valueToEdit.setDefaultFlagColor(valueDefinitionToEdit.getDefaultFlagColor());

        List<HsConditionColumn> columnsToLink = hsConditionColumnRepository.findBySectionTypes(valueDefinitionToEdit.getSections());
        if (columnsToLink.isEmpty()) {
            hsConditionColumnValueRepository.delete(valueToEdit);
        } else {
            CollectionDiffResult<HsConditionColumn, HsConditionColumn> diff = CspUtils.diffCollections(
                    valueToEdit.getConditionColumns(),
                    columnsToLink,
                    (column, column2) -> column.getId() != null
                            && column2.getId() != null
                            && column.getId().equals(column2.getId())
            );
            diff.getAdded().forEach(valueToEdit::addConditionColumn);
            diff.getRemoved().forEach(valueToEdit::removeConditionColumn);
            hsConditionColumnValueRepository.save(valueToEdit);
        }
    }

    @RequestMapping(value = "/healthSections/client/{clientId}/session/{sessionId}/rows", method = RequestMethod.GET)
    @Secured("ROLE_HEALTH")
    public Collection<HsRow> getAllClientSessionRows(@PathVariable("clientId") Long clientId,
                                                     @PathVariable("sessionId") Long sessionId) {

        return hsRowRepository.findByClientIdAndSessionId(clientId, sessionId);
    }

    @RequestMapping(value = "/healthSection/{sectionType}/client/{clientId}/session/{sessionId}/rows", method = RequestMethod.GET)
    @Secured("ROLE_HEALTH")
    public Collection<HsRow> getSectionClientSessionRows(@PathVariable("sectionType") String sectionType,
                                                         @PathVariable("clientId") Long clientId,
                                                         @PathVariable("sessionId") Long sessionId) {

        return hsRowRepository.findBySectionTypeAndClientIdAndSessionId(HsTableType.valueOf(sectionType), clientId, sessionId);
    }

    @RequestMapping(value = "/healthSection/{sectionType}/client/{clientId}/session/{sessionId}/row", method = RequestMethod.POST)
    @Secured({"ROLE_HEALTH", "ROLE_HEALTH_CONDITIONS"})
    public IdResponse postSessionClientSessionRow(@PathVariable("sectionType") String sectionType,
                                                  @PathVariable("clientId") Long clientId,
                                                  @PathVariable("sessionId") Long sessionId,
                                                  @RequestBody HsRow row) {

        HsTableType section = HsTableType.valueOf(sectionType);
        if (row.getValues().size() == 0) {// don't duplicate empty rows
            Optional<HsRow> firstEmpty = hsRowRepository
                    .findBySectionTypeAndClientIdAndSessionId(section, clientId, sessionId)
                    .stream()
                    .filter(hsRow -> hsRow.getValues().size() == 0)
                    .findFirst();
            if (firstEmpty.isPresent()) {
                return new IdResponse(firstEmpty.get().getId());
            }
        }

        row.setSection(section);
        row.setClientId(clientId);
        row.setSessionId(sessionId);

        Set<HsRowItem> values = row.getValues();
        row.setValues(new HashSet<>());
        values.forEach(row::addValue);

        HsRow saved = hsRowRepository.save(row);

        return new IdResponse(saved.getId());
    }

    @RequestMapping(value = "/healthSections/row/{rowId}", method = RequestMethod.GET)
    @Secured("ROLE_HEALTH")
    public HsRow getRow(@PathVariable("rowId") Long rowId) {

        return hsRowRepository.findById(rowId)
                .orElseThrow(() -> new NotFoundException("No such HsRowItem"));
    }


    @RequestMapping(value = "/healthSections/row/{rowId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK, reason = "Deleted")
    @Secured("ROLE_HEALTH_CONDITIONS")
    public void deleteRow(@PathVariable("rowId") Long rowId) {

        HsRow hsRow = hsRowRepository.findById(rowId)
                .orElseThrow(() -> new NotFoundException("No such HsRowItem"));

        hsRowRepository.delete(hsRow);
    }

    @RequestMapping(value = "/healthSections/row/{rowId}/column/{columnId}/value", method = RequestMethod.PUT)
    @Secured("ROLE_HEALTH_CONDITIONS")
    public void putSessionClientColumnValue(@PathVariable("rowId") Long rowId,
                                            @PathVariable("columnId") Long columnId,
                                            @RequestBody HsRowItem request) {

        HsRow hsRow = hsRowRepository.findById(rowId)
                .orElseThrow(() -> new NotFoundException("No such HsRowItem"));

        Optional<HsRowItem> itemOptional = hsRow.getValues().stream()
                .filter(hsRowItem -> hsRowItem.getColumnId() == columnId)
                .findFirst();

        HsColumn column = hsColumnRepository.findById(columnId)
                .orElseThrow(() -> new NotFoundException("No such HsColumn"));

        if (itemOptional.isPresent()) {
            HsRowItem rowItem = itemOptional.get();
            rowItem.setTextValue(request.getTextValue());
            rowItem.setSelectValue(request.getSelectValue());
            rowItem.setColorValue(request.getColorValue());
            hsRowItemRepository.save(rowItem);
        } else {
            HsRowItem rowItem = new HsRowItem();
            rowItem.setColumnId(columnId);
            rowItem.setTextValue(request.getTextValue());
            rowItem.setSelectValue(request.getSelectValue());
            rowItem.setColorValue(request.getColorValue());
            hsRow.addValue(rowItem);
            hsRowRepository.save(hsRow);
        }

        HsTableType sectionType = hsRow.getSection();
        // change protocol cost
        String columnTitle = column.getTitle();
        if ((sectionType == HsTableType.protocols || sectionType == HsTableType.packages)
                && (columnTitle.equals("Protocols Package") || columnTitle.equals("Protocols Package"))) {

            changeProtocolCost(sectionType, hsRow, request.getSelectValue());
        }
    }

    private void changeProtocolCost(HsTableType sectionType, HsRow hsRow, long protocolId) {

        HsColumn costColumn = hsColumnRepository.findBySectionTypeAndTitle(sectionType, "Cost to Client")
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No such costColumn"));

        Long costColumnId = costColumn.getId();

        Optional<HsRowItem> coastHsRowItemOp = hsRow.getValues()
                .stream()
                .filter(hsRowItem -> hsRowItem.getColumnId() == costColumnId)
                .findFirst();

        // get cost
        Protocol protocol = protocolRepository.findAll()
                .stream()
                .filter(proto -> proto.getExternalId() == protocolId)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No such Protocol"));
        String cost = protocol.getPrice();


        if (coastHsRowItemOp.isPresent()) {
            HsRowItem costRowItem = coastHsRowItemOp.get();
            costRowItem.setTextValue(cost);
            hsRowItemRepository.save(costRowItem);
        } else {
            HsRowItem costRowItem = new HsRowItem();
            HsRowItem.PK id = new HsRowItem.PK();
            id.setColumnId(costColumnId);
            id.setRow(hsRow);
            costRowItem.setId(id);
            costRowItem.setTextValue(cost);
            hsRowItemRepository.save(costRowItem);
        }

    }
}
