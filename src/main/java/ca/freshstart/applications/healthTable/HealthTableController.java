package ca.freshstart.applications.healthTable;

import ca.freshstart.types.AbstractController;
import ca.freshstart.data.session.entity.Session;
import ca.freshstart.data.healthSection.entity.HsColumn;
import ca.freshstart.data.healthSection.entity.HsRow;
import ca.freshstart.data.healthTable.entity.*;
import ca.freshstart.exceptions.BadRequestException;
import ca.freshstart.exceptions.NotFoundException;
import ca.freshstart.types.IdResponse;
import ca.freshstart.types.StatusRequest;
import ca.freshstart.types.StatusResponse;
import ca.freshstart.data.healthSection.types.HsColumnType;
import ca.freshstart.data.healthSection.types.HsTableType;
import ca.freshstart.data.healthTable.types.HealthTableColumnType;
import ca.freshstart.data.healthTable.types.HtColumnDefinition;
import ca.freshstart.data.session.repository.SessionRepository;
import ca.freshstart.data.healthSection.repository.HsColumnRepository;
import ca.freshstart.data.healthSection.repository.HsRowRepository;
import ca.freshstart.data.healthTable.repository.HealthTableColumnRepository;
import ca.freshstart.data.healthTable.repository.HealthTableCustomColumnRepository;
import ca.freshstart.data.healthTable.repository.HtRowItemRepository;
import ca.freshstart.data.healthTable.repository.HtRowRepository;
import ca.freshstart.helpers.CspUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class HealthTableController extends AbstractController {

    private final HealthTableColumnRepository healthTableColumnRepository;
    private final HealthTableCustomColumnRepository healthTableCustomColumnRepository;
    private final SessionRepository sessionRepository;
    private final HtRowRepository htRowRepository;
    private final HtRowItemRepository htRowItemRepository;
    private final HsRowRepository hsRowRepository;
    private final HsColumnRepository hsColumnRepository;

    @RequestMapping(value = "/healthTable/columns", method = RequestMethod.GET)
    @Secured("ROLE_HEALTH")
    public Collection<HtColumn> getHtColumns() {
        return healthTableColumnRepository.findPositioned();
    }

    @RequestMapping(value = "/healthTable/columns", method = RequestMethod.PUT)
    @Secured("ROLE_HEALTH_UI")
    public void putSectionColumns(@RequestBody HtColumnDefinition[] columnDefinitions) {
        for (HtColumnDefinition definition : columnDefinitions) {
            HtColumn column = healthTableColumnRepository.findById(definition.getId())
                    .orElseThrow(() -> new NotFoundException("No such HtColumn"));

            column = fillColumnFromDefinition(column, definition);

            healthTableColumnRepository.save(column);
        }
    }

    @RequestMapping(value = "/healthTable/column", method = RequestMethod.POST)
    @Secured("ROLE_HEALTH_UI")
    public IdResponse postColumn(@RequestBody HtColumnDefinition definition) {
        HealthTableColumnType columnType = HealthTableColumnType.valueOf(definition.getType());

        List<HtColumn> hidden;
        if (columnType == HealthTableColumnType.custom){
            hidden = healthTableColumnRepository.findNotPositionedByTypeAndCustomColumnId(columnType, definition.getCustomColumnId());
        } else {
            hidden = healthTableColumnRepository.findNotPositionedByType(columnType);
        }

        HtColumn column;
        if (hidden.isEmpty()) {
            column = new HtColumn();
        } else {
            column = hidden.get(0);
        }

        column = fillColumnFromDefinition(column, definition);

        HtColumn savedColumn = healthTableColumnRepository.save(column);

        return new IdResponse(savedColumn.getId());
    }

    private HtColumn fillColumnFromDefinition(HtColumn column, HtColumnDefinition definition) {
        HealthTableColumnType columnType = HealthTableColumnType.valueOf(definition.getType());

        column.setType(columnType);
        column.setTitle(definition.getTitle());
        column.setPosition(definition.getPosition());

        Long customColumnId = definition.getCustomColumnId();
        if (columnType == HealthTableColumnType.custom && customColumnId != null) {
            HtCustomColumn customColumn = healthTableCustomColumnRepository.findById(customColumnId)
                    .orElseThrow(() -> new NotFoundException("No such HtCustomColumn"));
            column.setCustomColumn(customColumn);
        }

        return column;
    }

    @RequestMapping(value = "/healthTable/column/{columnId}", method = RequestMethod.PUT)
    @Secured("ROLE_HEALTH_UI")
    public void putColumn(@PathVariable("columnId") Long columnId,
                          @RequestBody HtColumnDefinition columnDefinition) {
        if (!columnDefinition.getId().equals(columnId))
            throw new BadRequestException("HtColumn.id does not match columnId in path parameter");

        HtColumn column = healthTableColumnRepository.findById(columnId)
                .orElseThrow(() -> new NotFoundException("No such HtColumn"));

        HealthTableColumnType columnType = HealthTableColumnType.valueOf(columnDefinition.getType());
        column.setType(columnType);
        column.setTitle(columnDefinition.getTitle());
        column.setPosition(columnDefinition.getPosition());

        if (columnType == HealthTableColumnType.custom && columnDefinition.getCustomColumnId() != null) {
            HtCustomColumn customColumn = healthTableCustomColumnRepository.findById(columnDefinition.getCustomColumnId())
                    .orElseThrow(() -> new NotFoundException("No such HtCustomColumn"));
            column.setCustomColumn(customColumn);
        } else {
            column.setCustomColumn(null);
        }

        healthTableColumnRepository.save(column);
    }

    @RequestMapping(value = "/healthTable/column/{columnId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK, reason = "Deleted")
    @Secured("ROLE_HEALTH_UI")
    public void deleteColumn(@PathVariable("columnId") Long columnId) {
        HtColumn column = healthTableColumnRepository.findById(columnId)
                .orElseThrow(() -> new NotFoundException("No such HtColumn"));

        column.setPosition(null);
        healthTableColumnRepository.save(column);
    }

    @RequestMapping(value = "/healthTable/customColumns", method = RequestMethod.GET)
    @Secured("ROLE_HEALTH")
    public Collection<HtCustomColumn> getHtCustomColumns() {
        return healthTableCustomColumnRepository.findAll();
    }

    @RequestMapping(value = "/healthTable/customColumn", method = RequestMethod.POST)
    @Secured("ROLE_HEALTH_UI")
    public IdResponse postCustomColumn(@RequestBody HtCustomColumn customColumn) {
        HtCustomColumn savedCustomColumn = healthTableCustomColumnRepository.save(customColumn);
        return new IdResponse(savedCustomColumn.getId());
    }

    @RequestMapping(value = "/healthTable/customColumn/{columnId}", method = RequestMethod.PUT)
    @Secured("ROLE_HEALTH_UI")
    public void putCustomColumn(@PathVariable("columnId") Long columnId,
                                @RequestBody HtCustomColumn customColumn) {
        long id = customColumn.getId();
        if (id == columnId) {
            healthTableCustomColumnRepository.save(customColumn);
        } else {
            throw new BadRequestException("HtCustomColumn.id does not match columnId in path parameter");
        }
    }

    @RequestMapping(value = "/healthTable/customColumn/{columnId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK, reason = "Deleted")
    @Secured("ROLE_HEALTH_UI")
    public void deleteCustomColumn(@PathVariable("columnId") Long columnId) {
        HtCustomColumn customColumn = healthTableCustomColumnRepository.findById(columnId)
                .orElseThrow(() -> new NotFoundException("No such HtCustomColumn"));
        healthTableCustomColumnRepository.delete(customColumn);
    }

    @RequestMapping(value = "/healthTable/customColumn/{columnId}/values", method = RequestMethod.PUT)
    @Secured("ROLE_HEALTH_UI")
    public void putCustomColumnValues(@PathVariable("columnId") Long columnId,
                                      @RequestBody HtCustomColumnValue[] newValues) {

        HtCustomColumn customColumn = healthTableCustomColumnRepository.findById(columnId)
                .orElseThrow(() -> new NotFoundException("No such HtCustomColumn"));

        CspUtils.mergeValues(
                customColumn.getValues(),
                Arrays.asList(newValues),
                customColumn::removeValue,
                customColumn::addValue,
                (to, from) -> {
                    to.setTitle(from.getTitle());
                    to.setValue(from.getValue());
                    to.setPosition(from.getPosition());
                });

        healthTableCustomColumnRepository.save(customColumn);
    }

    @RequestMapping(value = "/healthTable/session/{sessionId}/data", method = RequestMethod.GET)
    @Secured("ROLE_HEALTH")
    public Collection<HtRow> getSessionData(@PathVariable("sessionId") Long sessionId) {

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("No such Session"));

        // find Health Table Rows only for clients which present in Session
        List<HtRow.PK> htRowIds = session.getClients().stream().map(client -> {
            HtRow.PK pk = new HtRow.PK();
            pk.setSessionId(sessionId);
            pk.setClientId(client.getId());
            return pk;
        }).collect(Collectors.toList());
        List<HtRow> clientDataList = htRowRepository.findByIds(htRowIds);
//        List<HtRow> clientDataList = htRowRepository.findBySessionId(sessionId);

        // Flag columns in Health Table
        List<Long> htFlagColumnIds = healthTableColumnRepository.findByType(HealthTableColumnType.flag).stream()
                .map(HtColumn::getId)
                .collect(Collectors.toList());

        // Fill flag values in Client Data
        clientDataList.forEach(clientData -> {
            long clientId = clientData.getClientId();

            Set<String> colorValues = new HashSet<>();
            // Process only sections witch can contains flag columns
            HsTableType[] sectionsWithFlag = {HsTableType.physical, HsTableType.emotional, HsTableType.structural};
            Arrays.stream(sectionsWithFlag).forEach(sectionType -> {
                List<Long> hsFlagColumnIds = hsColumnRepository.findBySectionTypeAndType(sectionType, HsColumnType.flag).stream()
                        .map(HsColumn::getId)
                        .collect(Collectors.toList());
                List<HsRow> hsRows = hsRowRepository.findBySectionTypeAndClientIdAndSessionId(sectionType, clientId, sessionId);

                hsRows.forEach(hsRow -> {
                    hsRow.getValues().forEach(hsRowItem -> {
                        long columnId = hsRowItem.getColumnId();
                        boolean isFlagItem = hsFlagColumnIds.stream()
                                .anyMatch(flagColumnId -> flagColumnId == columnId);
                        if (isFlagItem)
                            colorValues.add(hsRowItem.getColorValue());
                    });
                });

            });

            // Make flag value for Health Table
            StringBuilder builder = new StringBuilder("");
            colorValues.forEach(s -> {
                if (s != null) {
                    if (builder.length() > 0) {
                        builder.append(" ");
                    }
                    builder.append(s);
                }
            });
            String flagValues = builder.toString();

            // Set colors value
//            if (!flagValues.isEmpty()) {
            clientData.getValues().forEach(clientDataItem -> {
                long itemColumnId = clientDataItem.getColumnId();
                boolean isFlagColumn = htFlagColumnIds.stream()
                        .anyMatch(htFlagColumnId -> htFlagColumnId == itemColumnId);
                if (isFlagColumn)
                    clientDataItem.setTextValue(flagValues);
            });
        });

        return clientDataList;
    }


    @RequestMapping(value = "/healthTable/session/{sessionId}/client/{clientId}/status", method = RequestMethod.GET)
    @Secured("ROLE_HEALTH")
    public StatusResponse getSessionClientStatus(@PathVariable("sessionId") Long sessionId,
                                                 @PathVariable("clientId") Long clientId) {
        HtRowItem statusDataItem = getStatusDataItem(sessionId, clientId);
        return new StatusResponse() {{
            setStatus(statusDataItem.getTextValue());
        }};
    }

    @RequestMapping(value = "/healthTable/session/{sessionId}/client/{clientId}/status", method = RequestMethod.PUT)
    @Secured("ROLE_HEALTH_CONDITIONS")
    public void putSessionClientStatus(@PathVariable("sessionId") Long sessionId,
                                       @PathVariable("clientId") Long clientId,
                                       @RequestBody StatusRequest statusRequest) {
        HtRowItem statusDataItem = getStatusDataItem(sessionId, clientId);
        statusDataItem.setTextValue(statusRequest.getStatus());
        htRowItemRepository.save(statusDataItem);
    }

    private HtRowItem getStatusDataItem(Long sessionId, Long clientId) {
        List<HtColumn> statusColumns = healthTableColumnRepository.findByType(HealthTableColumnType.status);
        if (statusColumns.isEmpty())
            throw new NotFoundException("No such HtColumn of status column");

        long statusColumnId = statusColumns.get(0).getId();
        return htRowItemRepository.findByColumnIdAndSessionIdAndClientId(sessionId, clientId, statusColumnId)
                .orElseThrow(() -> new NotFoundException("No such HtRowItem of status column"));
    }

    @RequestMapping(value = "/healthTable/session/{sessionId}/client/{clientId}/column/{columnId}/value", method = RequestMethod.PUT)
    @Secured("ROLE_HEALTH_CONDITIONS")
    public void putSessionClientColumnValue(@PathVariable("sessionId") Long sessionId,
                                            @PathVariable("clientId") Long clientId,
                                            @PathVariable("columnId") Long columnId,
                                            @RequestBody HtRowItem request) {

        Optional<HtRowItem> itemOptional = htRowItemRepository.findByColumnIdAndSessionIdAndClientId(sessionId, clientId, columnId);

        if (!itemOptional.isPresent()) {
            HtRow.PK id = new HtRow.PK();
            id.setClientId(clientId);
            id.setSessionId(sessionId);
            HtRow clientData = htRowRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("No such HtRow"));

            HtRowItem clientDataItem = new HtRowItem();
            clientDataItem.setColumnId(columnId);
            clientDataItem.setTextValue(request.getTextValue());
            clientDataItem.setSelectValue(request.getSelectValue());
            clientData.addValue(clientDataItem);
            htRowRepository.save(clientData);
        } else {
            HtRowItem clientDataItem = itemOptional.get();
            clientDataItem.setTextValue(request.getTextValue());
            clientDataItem.setSelectValue(request.getSelectValue());
            htRowItemRepository.save(clientDataItem);
        }
    }
}