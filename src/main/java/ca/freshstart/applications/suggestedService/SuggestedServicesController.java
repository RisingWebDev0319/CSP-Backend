package ca.freshstart.applications.suggestedService;

import ca.freshstart.data.client.entity.Client;
import ca.freshstart.data.client.repository.ClientRepository;
import ca.freshstart.data.session.entity.Session;
import ca.freshstart.data.session.repository.SessionRepository;
import ca.freshstart.types.AbstractController;
import ca.freshstart.data.suggestions.entity.*;
import ca.freshstart.data.service.entity.Service;

import ca.freshstart.exceptions.BadRequestException;
import ca.freshstart.exceptions.ConflictException;
import ca.freshstart.exceptions.NotFoundException;
import ca.freshstart.types.IdResponse;
import ca.freshstart.data.suggestions.types.SuggestedServicesColumnType;
import ca.freshstart.data.service.repository.ServiceRepository;
import ca.freshstart.data.suggestions.repository.*;
import ca.freshstart.helpers.ModelValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Date;

@RestController
@RequiredArgsConstructor
public class SuggestedServicesController extends AbstractController {

    private final SsTableColumnRepository suggestedColumnRepository;
    private final SsCustomColumnRepository suggestedCustomColumnRepository;
    private final SsSessionClientDataRepository sessionClientDataRepository;
    private final SsSessionClientDataItemRepository sessionClientDataItemRepository;
    private final PreliminaryEventRepository preliminaryEventRepository;
    private final ServiceRepository serviceRepository;
    private final ClientRepository clientRepository;
    private final SessionRepository sessionRepository;

    /**
     * Return list of columns for suggested services table
     *
     * @return Requested columns info
     */
    @RequestMapping(value = "/suggestedServicesTable/columns", method = RequestMethod.GET)
    @Secured({"ROLE_SUGGESTED_SERVICES", "ROLE_SUGGESTED_SERVICES_UI"})
    public Collection<SsTableColumn> getColumns() {
        return suggestedColumnRepository.findAll();
    }

    /**
     * Return list of rows of client data for session
     *
     * @param sessionId id of session
     * @return Requested data
     */
    @RequestMapping(value = "/suggestedServices/session/{sessionId}/data", method = RequestMethod.GET)
    @Secured({"ROLE_SUGGESTED_SERVICES", "ROLE_SUGGESTED_SERVICES_UI"})
    public Collection<SsSessionClientData> getSessionData(@PathVariable("sessionId") Long sessionId) {
        return sessionClientDataRepository.findBySessionId(sessionId);
    }

    /**
     * Add new column in table to show
     *
     * @param definition ...
     * @return id
     */
    @RequestMapping(value = "/suggestedServices/column", method = RequestMethod.POST)
    @Secured({"ROLE_SUGGESTED_SERVICES_UI"})
    public IdResponse postColumn(@RequestBody SsColumnDefinition definition) {
        ModelValidation.validateSsColumnDefinition(definition);

        List<SsTableColumn> columns = suggestedColumnRepository.findByTypeAndCustomColumnId(definition.getType(), definition.getCustomColumnId());

        SsTableColumn column = null;
        if (columns.isEmpty()) {
            column = new SsTableColumn();
        } else {
            column = columns.get(0);
        }

        // Bad requests - wrong column or duplicate ??

        column = fillColumnFromDefinition(column, definition);

        SsTableColumn savedColumn = suggestedColumnRepository.save(column);

        return new IdResponse(savedColumn.getId());
    }

    private SsTableColumn fillColumnFromDefinition(SsTableColumn column, SsColumnDefinition definition) {
        SuggestedServicesColumnType columnType = definition.getType();

        column.setType(columnType);
        column.setTitle(definition.getTitle());
//        column.setPosition(definition.getPosition());

        Long customColumnId = definition.getCustomColumnId();

        if (columnType == SuggestedServicesColumnType.custom && customColumnId != null) {
            SsCustomColumn customColumn = suggestedCustomColumnRepository.findById(customColumnId)
                    .orElseThrow(() -> new NotFoundException("No such SuggestedServicesCustomColumn"));

            column.setCustomColumn(customColumn);
        }

        return column;
    }

    /**
     * Delete column from table
     *
     * @param columnId id of column
     */
    @RequestMapping(value = "/suggestedServices/column/{columnId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK, reason = "Deleted")
    @Secured("ROLE_SUGGESTED_SERVICES_UI")
    public void deleteColumn(@PathVariable("columnId") Long columnId) {
        SsTableColumn column = suggestedColumnRepository.findById(columnId)
                .orElseThrow(() -> new NotFoundException("No such column"));

        suggestedColumnRepository.delete(column);
    }

    /**
     * Update column
     *
     * @param columnId   id of column
     * @param definition ...
     */
    @RequestMapping(value = "/suggestedServices/column/{columnId}", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK, reason = "Updated")
    @Secured("ROLE_SUGGESTED_SERVICES_UI")
    public void updateColumn(@PathVariable("columnId") Long columnId, @RequestBody SsColumnDefinition definition) {

        SsTableColumn column = suggestedColumnRepository.findById(columnId)
                .orElseThrow(() -> new NotFoundException("No such column"));

        column.setTitle(definition.getTitle());

        SsTableColumn column1 = fillColumnFromDefinition(column, definition);

        // 400
//        Bad requests - wrong column or duplicate

        // todo update rest fields

        try {
            suggestedColumnRepository.save(column);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Name should be unique and not empty");
        }
    }

    @RequestMapping(value = "/suggestedServices/session/{sessionId}/client/{clientId}/column/{columnId}/value", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK, reason = "Updated")
    @Secured({"ROLE_SUGGESTED_SERVICES", "ROLE_SUGGESTED_SERVICES_UI"})
    public void update(@PathVariable("sessionId") Long sessionId,
                       @PathVariable("clientId") Long clientId,
                       @PathVariable("columnId") Long columnId,
                       @RequestBody SsSessionClientDataItem request) {

        Optional<SsSessionClientDataItem> itemOptional = sessionClientDataItemRepository.findByColumnIdAndSessionIdAndClientId(sessionId, clientId, columnId);

        Client client = clientRepository.findOne(clientId);
        Session session = sessionRepository.findOne(sessionId);

        if (!itemOptional.isPresent()) {
            Optional<SsSessionClientData> clientDataOptional = sessionClientDataRepository.findById(new SsSessionClientData.PK() {{
                setClient(client);
                setSession(session);
            }});

            SsSessionClientData clientData;
            if (clientDataOptional.isPresent()) {
                clientData = clientDataOptional.get();
            } else {
                clientData = new SsSessionClientData();
                clientData.setClient(client);
                clientData.setSession(session);
            }

            SsSessionClientDataItem clientDataItem = new SsSessionClientDataItem();

            clientDataItem.setColumnId(columnId);
            clientDataItem.setTextValue(request.getTextValue());
            clientDataItem.setSelectValue(request.getSelectValue());

            clientData.addValue(clientDataItem);

            sessionClientDataRepository.save(clientData);
        } else {
            SsSessionClientDataItem clientDataItem = itemOptional.get();

            clientDataItem.setTextValue(request.getTextValue());
            clientDataItem.setSelectValue(request.getSelectValue());

            sessionClientDataItemRepository.save(clientDataItem);
        }
    }

    /**
     * Return all services in session added to client
     *
     * @param clientId  client id
     * @param sessionId session id
     * @return services
     */
    @RequestMapping(value = "/suggestedServices/session/{sessionId}/client/{clientId}/services", method = RequestMethod.GET)
    @Secured("ROLE_SUGGESTED_SERVICES_UI")
    public Collection<PreliminaryEvent> getSessionClientSessionRows(@PathVariable("sessionId") Long sessionId,
                                                                    @PathVariable("clientId") Long clientId) {

        return preliminaryEventRepository.findBySessionIdAndClientId(sessionId, clientId);
        // No such session/client or client not assigned to session 404
    }

    /**
     * Add new service row to client
     *
     * @param clientId  client id
     * @param sessionId session id
     */
    @RequestMapping(value = "/suggestedServices/session/{sessionId}/client/{clientId}/service", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK, reason = "The event has been created successfully.")
    @Secured("ROLE_SUGGESTED_SERVICES_UI")
    public void postClientServiceDataItem(@PathVariable("sessionId") Long sessionId,
                                          @PathVariable("clientId") Long clientId,
                                          @RequestBody PreliminaryEvent request) {

        Client client = clientRepository.findOne(clientId);
        if (client == null) {
            throw new NotFoundException("No such client");
        }

        Session session = sessionRepository.findOne(sessionId);
        if (session == null) {
            throw new NotFoundException("No such session");
        }

        Service service = request.getService();
        if (service == null) {
            throw new BadRequestException("PreliminaryEvent have not service");
        }

        preliminaryEventRepository.findBySessionIdAndClientIdAndServiceIdAndDate(sessionId, clientId, service.getId(), request.getDate())
                .ifPresent(item -> {
                    throw new BadRequestException("PreliminaryEvent already exist");
                });

        if (request.getDate() == null) {
            throw new BadRequestException("Date of PreliminaryEvent can't be null");
        }

        PreliminaryEvent reqBody = new PreliminaryEvent();
        reqBody.setService(service);
        reqBody.setRoom(request.getRoom());
        reqBody.setTherapist(request.getTherapist());
        reqBody.setDate(request.getDate());
        reqBody.setNote(request.getNote());
        reqBody.setDuration(service.getTime());

        Optional<SsSessionClientData> clientDataOptional = sessionClientDataRepository.findById(new SsSessionClientData.PK() {{
            setClient(client);
            setSession(session);
        }});

        SsSessionClientData clientData = new SsSessionClientData();

        if (clientDataOptional.isPresent()) {
            clientData = clientDataOptional.get();

            if (clientData == null) {
                throw new ConflictException("ClientData is null.");
            }
            //Save PreliminaryService there
            reqBody.setDataFK(clientData);
//            clientData.addService(request);
            try {
                preliminaryEventRepository.save(reqBody);
            } catch (DataIntegrityViolationException ex) {
                throw new ConflictException("Creating preliminary event completed with error.");
            }


        } else {
            clientData.setClient(client);
            clientData.setSession(session);
            clientData.addService(reqBody);
            sessionClientDataRepository.save(clientData);
        }
    }

    @RequestMapping(value = "/suggestedServices/session/{sessionId}/client/{clientId}/service/{serviceId}", method = RequestMethod.GET)
    @Secured({"ROLE_SUGGESTED_SERVICES", "ROLE_SUGGESTED_SERVICES_UI"})
    public List<PreliminaryEvent> GET_IT(@PathVariable("sessionId") Long sessionId,
                                         @PathVariable("clientId") Long clientId,
                                         @PathVariable("serviceId") Long serviceId) {
        List<PreliminaryEvent> items = preliminaryEventRepository.findBySessionIdAndClientIdAndServiceId(sessionId, clientId, serviceId);
        if (items.size() < 1)
            throw new NotFoundException("No such PreliminaryEvent");
        return items;
    }


    /**
     * Update service row to client
     *
     * @param clientId  client id
     * @param sessionId session id
     * @param serviceId service id
     */
    @RequestMapping(value = "/suggestedServices/session/{sessionId}/client/{clientId}/service/{serviceId}", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK, reason = "Updated")
    @Secured({"ROLE_SUGGESTED_SERVICES", "ROLE_SUGGESTED_SERVICES_UI"})
    public void PUT_IT(@PathVariable("sessionId") Long sessionId,
                       @PathVariable("clientId") Long clientId,
                       @PathVariable("serviceId") Long serviceId,
                       @RequestBody PreliminaryEvent request) {

        Optional<PreliminaryEvent> clientDataItem = preliminaryEventRepository.findById(request.getId());
        clientDataItem.orElseThrow(() -> new NotFoundException("No such PreliminaryEvent"));

        PreliminaryEvent preItem = clientDataItem.get();
        preItem.setDate(request.getDate());
        preItem.setNote(request.getNote());
        preItem.setRoom(request.getRoom());
        preItem.setTherapist(request.getTherapist());

        preliminaryEventRepository.save(preItem);
    }

    /**
     * Delete service row
     *
     * @param clientId  client id
     * @param sessionId session id
     * @param serviceId service id
     */
    @RequestMapping(value = "/suggestedServices/preliminaryEvent/{Id}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK, reason = "Deleted")
    @Secured({"ROLE_SUGGESTED_SERVICES", "ROLE_SUGGESTED_SERVICES_UI"})
    public void DELETE_IT(@PathVariable("Id") Long Id) {

        PreliminaryEvent clientDataItem = preliminaryEventRepository.findById(Id)
                .orElseThrow(() -> new NotFoundException("No such PreliminaryEvent"));

        SsSessionClientData ssSessionClientData = sessionClientDataRepository.findById(new SsSessionClientData.PK() {{
            setClient(clientDataItem.getClient());
            setSession(clientDataItem.getSession());
        }}).orElseThrow(() -> new NotFoundException("No such SsSessionClientData"));

        List<PreliminaryEvent> events = preliminaryEventRepository.findBySessionIdAndClientIdAndServiceId(clientDataItem.getSession().getId(), clientDataItem.getClient().getId(), clientDataItem.getService().getId());
        if (events.size() == 1) {
            ssSessionClientData.removeService(clientDataItem);
            sessionClientDataRepository.save(ssSessionClientData);
        }
        try {
            preliminaryEventRepository.deleteById(clientDataItem.getId());
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Removing Preliminary event completed with Error.");
        }

    }
}
