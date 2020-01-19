package ca.freshstart.applications.session;

import ca.freshstart.types.AbstractController;
import ca.freshstart.data.client.entity.Client;
import ca.freshstart.data.session.entity.Session;
import ca.freshstart.data.healthSection.entity.HsColumn;
import ca.freshstart.data.healthSection.entity.HsRow;
import ca.freshstart.data.healthSection.entity.HsRowItem;
import ca.freshstart.data.healthTable.entity.HtColumn;
import ca.freshstart.data.healthTable.entity.HtRow;
import ca.freshstart.data.healthTable.entity.HtRowItem;
import ca.freshstart.exceptions.ConflictException;
import ca.freshstart.exceptions.NotFoundException;
import ca.freshstart.types.Count;
import ca.freshstart.types.IdResponse;
import ca.freshstart.data.healthSection.types.HsTableType;
import ca.freshstart.data.healthTable.types.HealthTableClientStatus;
import ca.freshstart.data.healthTable.types.HealthTableColumnType;
import ca.freshstart.data.session.repository.SessionRepository;
import ca.freshstart.data.healthSection.repository.HsColumnRepository;
import ca.freshstart.data.healthSection.repository.HsRowRepository;
import ca.freshstart.data.healthTable.repository.HealthTableColumnRepository;
import ca.freshstart.data.healthTable.repository.HtRowRepository;
import ca.freshstart.helpers.CspUtils;
import ca.freshstart.helpers.ModelValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class SessionController extends AbstractController {

    protected final SessionRepository sessionRepository;
    private final HtRowRepository htRowRepository;
    private final HealthTableColumnRepository healthTableColumnRepository;
    private final HsColumnRepository hsColumnRepository;
    private final HsRowRepository hsRowRepository;

    /**
     * Return list of sessions
     *
     * @param pageId   page to show
     * @param pageSize Item on page to show (10 by default)
     * @param sort     Field to sort by, +/- prefix defines order (ACS/DESC)
     * @return Requested sessions
     */
    @RequestMapping(value = "/sessions", method = RequestMethod.GET)
    @Secured("ROLE_SESSIONS")
    public Collection<Session> list(@RequestParam(value = "pageId", required = false, defaultValue = "1") int pageId,
                                    @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                                    @RequestParam(value = "sort", required = false) String sort) {

        return sessionRepository.findAllAsList(new PageRequest(pageId - 1, pageSize, CspUtils.createSort(sort)));
    }

    /**
     * Return count of sessions
     *
     * @return Count of sessions
     */
    @RequestMapping(value = "/sessions/count", method = RequestMethod.GET)
    @Secured("ROLE_SESSIONS")
    public Count count() {
        return new Count(sessionRepository.count());
    }

    /**
     * Return list of archived sessions
     *
     * @param pageId   page to show
     * @param pageSize Item on page to show (10 by default)
     * @param sort     Field to sort by, +/- prefix defines order (ACS/DESC)
     * @return Requested archived sessions
     */
    @RequestMapping(value = "/sessions/archive", method = RequestMethod.GET)
    @Secured("ROLE_SESSIONS")
    public Collection<Session> listArchive(@RequestParam(value = "pageId", required = false, defaultValue = "1") int pageId,
                                           @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                                           @RequestParam(value = "sort", required = false) String sort) {
        return sessionRepository.findAllArchived(new PageRequest(pageId - 1, pageSize, CspUtils.createSort(sort)));
    }

    /**
     * Return count of archived sessions
     *
     * @return Count of archived sessions
     */
    @RequestMapping(value = "/sessions/archive/count", method = RequestMethod.GET)
    @Secured("ROLE_SESSIONS")
    public Count countArchive() {
        return new Count(sessionRepository.countArchived());
    }

    /**
     * Add new session
     *
     * @param session session to add
     * @return Id of the session
     */
    @RequestMapping(value = "/session", method = RequestMethod.POST)
    @Secured("ROLE_SESSIONS")
    @Transactional
    public IdResponse addNew(@RequestBody Session session) {
        ModelValidation.validateSession(session);

        try {
            sessionRepository.save(session);
            updateHealthTableRows(session);
            updateHealthSections(session);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Name and dates should be unique and not empty");
        }

        return new IdResponse(session.getId());
    }

    /**
     * Changing the name, dates and object of session
     *
     * @param sessionFrom session to change
     */
    @RequestMapping(value = "/session/{sessionId}", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK, reason = "Updated")
    @Secured("ROLE_SESSIONS")
    @Transactional
    public void update(@PathVariable("sessionId") Long sessionId, @RequestBody Session sessionFrom) {
        ModelValidation.validateSession(sessionFrom);

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("No such session"));

        session.setName(sessionFrom.getName());
        session.setStartDate(sessionFrom.getStartDate());
        session.setEndDate(sessionFrom.getEndDate());

        session.setClients(sessionFrom.getClients());
        session.setTherapists(sessionFrom.getTherapists());

        try {
            sessionRepository.save(session);
            updateHealthTableRows(session);
            updateHealthSections(session);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Name and dates should be unique and not empty");
        }
    }

    private long getHtColumnIdByType(HealthTableColumnType type) {
        List<HtColumn> columns = healthTableColumnRepository.findByType(type);
        if (columns.isEmpty())
            throw new NotFoundException("No such HtColumn of type '" + type.name() + "'");
        return columns.get(0).getId();
    }

    private void updateHealthSections(Session session) {

        Long sessionId = session.getId();
        Set<Client> clients = session.getClients();

        Long goalFieldColumnId = getColumnIdByTitle("Goal Field", HsTableType.goals);
        Long clientDetailColumnId = getColumnIdByTitle("Client Details", HsTableType.goals);

        // find existed rows
        for (Client client : clients) {
            // update Goals section
            HashMap<String, Map<Long, String>> table = new HashMap<String, Map<Long, String>>() {{
                put("Goals", new HashMap<Long, String>() {{
                    put(clientDetailColumnId, client.getGoals());
                }});
                put("Reasons", new HashMap<Long, String>() {{
                    put(clientDetailColumnId, client.getReasons());
                }});
            }};
            updateHealthSection(sessionId, client.getId(), HsTableType.goals, goalFieldColumnId, table);
        }
    }

    private void updateHealthSection(Long sessionId, Long clientId, HsTableType type,
                                     Long keyColumnId, Map<String, Map<Long, String>> table) {

        List<HsRow> rows = hsRowRepository.findBySectionTypeAndClientIdAndSessionId(type, clientId, sessionId);

        table.keySet().forEach((String keyColumnValue) -> {
            Map<Long, String> tableRow = table.get(keyColumnValue);

            tableRow.keySet().forEach((Long columnId) -> {
                String columnValue = tableRow.get(columnId);

                if (!CspUtils.isNullOrEmpty(columnValue)) {
                    HsRow foundGoalsRow = updateSection(rows, sessionId, clientId, keyColumnId, keyColumnValue, columnId, columnValue);
                    if (foundGoalsRow != null) {
                        hsRowRepository.save(foundGoalsRow);
                    }
                }

            });
        });

    }

    private HsRow updateSection(List<HsRow> rows,
                                Long sessionId,
                                Long clientId,
                                Long keyColumnId,
                                String keyValue,
                                Long dataColumnId,
                                String dataValue) {
        return rows.stream()
                .filter(hsRow -> {
                    // 1) find row
                    Set<HsRowItem> items = hsRow.getValues();
                    HsRowItem keyItem = getItemValueByColumnId(keyColumnId, items);
                    if (keyItem == null) {
                        return false;
                    }
                    String keyText = keyItem.getTextValue();
                    if (CspUtils.isNullOrEmpty(keyText)) {
                        return false;
                    }
                    if (!keyText.equals(keyValue)) {
                        return false;
                    }

                    HsRowItem rowItem = getItemValueByColumnId(dataColumnId, items);
                    if (rowItem == null) {
                        rowItem = new HsRowItem();
                        rowItem.setColumnId(dataColumnId);
                        hsRow.addValue(rowItem);
                    }
                    rowItem.setTextValue(dataValue);
                    return true;
                })
                .findFirst()
                .orElseGet(() -> {
                    // 2) create if not exist
                    HsRow row = new HsRow();
                    row.setSection(HsTableType.goals);
                    row.setSessionId(sessionId);
                    row.setClientId(clientId);
                    {
                        HsRowItem rowItem = new HsRowItem();
                        rowItem.setColumnId(keyColumnId);
                        rowItem.setTextValue(keyValue);
                        row.addValue(rowItem);

                    }
                    {
                        HsRowItem rowItem = new HsRowItem();
                        rowItem.setColumnId(dataColumnId);
                        rowItem.setTextValue(dataValue);
                        row.addValue(rowItem);
                    }
                    return row;
                });
    }

    private HsRowItem getItemValueByColumnId(Long columnId, Set<HsRowItem> items) {
        return items.stream()
                .filter(hsRowItem -> hsRowItem.getColumnId() == columnId)
                .findFirst()
                .orElseGet(null);
    }

    private Long getColumnIdByTitle(String title, HsTableType type) {
        List<HsColumn> hsColumns = hsColumnRepository.findBySectionTypeAndTitle(type, title);
        if (hsColumns.isEmpty()) {
            throw new NotFoundException("No such column of Health Section with title '" + title + "' in HsColumn");
        }
        HsColumn hsColumn = hsColumns.get(0);
        return hsColumn.getId();
    }

    private void updateHealthTableRows(Session session) {
        long nameColumnId = getHtColumnIdByType(HealthTableColumnType.name);
        long flagColumnId = getHtColumnIdByType(HealthTableColumnType.flag);
        long statusColumnId = getHtColumnIdByType(HealthTableColumnType.status);

        Long sessionId = session.getId();
        Set<Client> clients = session.getClients();

        // find existed rows
        List<HtRow.PK> htRowIdList = clients.stream().map(client -> {
            HtRow.PK pk = new HtRow.PK();
            pk.setSessionId(sessionId);
            pk.setClientId(client.getId());
            return pk;
        }).collect(Collectors.toList());
        List<HtRow> existedHtRows = htRowRepository.findByIds(htRowIdList);

        // make hash table for fast search
        Map<Long, HtRow> clientId2HtRow = existedHtRows.stream().collect(Collectors.toMap(HtRow::getClientId, row -> row));

        List<HtRow> newHtRows = clients.stream()
                .map(client -> {
                    HtRow foundHtRow = clientId2HtRow.get(client.getId());
                    if (foundHtRow != null) {
                        // don't change existed
                        return null;
                    } else {
                        // only create new
                        HtRow htRow = new HtRow();
                        htRow.setSessionId(sessionId);
                        htRow.setClientId(client.getId());
                        {
                            HtRowItem nameValue = new HtRowItem();
                            nameValue.setColumnId(nameColumnId);
                            nameValue.setTextValue(client.getName());
                            htRow.addValue(nameValue);
                        }
                        {
                            HtRowItem flagValue = new HtRowItem();
                            flagValue.setColumnId(flagColumnId);
                            flagValue.setTextValue("");// will be replaced by recalculation from Health Sections flags
                            htRow.addValue(flagValue);
                        }
                        {
                            HtRowItem statusValue = new HtRowItem();
                            statusValue.setColumnId(statusColumnId);
                            statusValue.setTextValue(HealthTableClientStatus.not_started.name());
                            htRow.addValue(statusValue);
                        }
                        return htRow;
                    }

                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        htRowRepository.save(newHtRows);
    }

    /**
     * Return information about one session
     *
     * @param sessionId id of session
     * @return session data
     */
    @RequestMapping(value = "/session/{sessionId}", method = RequestMethod.GET)
    @Secured("ROLE_SESSIONS")
    public Session getById(@PathVariable("sessionId") Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("No such session"));
    }

    /**
     * Move session to archive
     *
     * @param sessionId id of session
     */
    @RequestMapping(value = "/session/{sessionId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK, reason = "Deleted")
    @Secured("ROLE_SESSIONS")
    public void deleteById(@PathVariable("sessionId") Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("No such session"));

        session.setArchived(true);

        sessionRepository.save(session);
    }
}
