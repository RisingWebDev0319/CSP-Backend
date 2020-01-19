package ca.freshstart.applications.matching;

import ca.freshstart.data.suggestions.repository.PreliminaryEventRepository;
import ca.freshstart.types.AbstractController;
import ca.freshstart.data.availability.entity.AvTherapistDayRecord;
import ca.freshstart.data.availability.entity.AvTimeRecord;
import ca.freshstart.data.client.entity.Client;
import ca.freshstart.data.concreteEvent.entity.ConcreteEvent;
import ca.freshstart.data.concreteCalendarEvent.entity.ConcreteCalendarEvent;
import ca.freshstart.data.matching.entity.ClientMatchingConfirmation;
import ca.freshstart.data.matching.types.*;
import ca.freshstart.data.reconcile.entity.ConcreteEventReconcile;
import ca.freshstart.data.room.entity.Room;
import ca.freshstart.data.service.entity.Service;
import ca.freshstart.data.session.entity.Session;
import ca.freshstart.data.therapist.entity.Therapist;
import ca.freshstart.data.suggestions.entity.PreliminaryEvent;
import ca.freshstart.exceptions.BadRequestException;
import ca.freshstart.exceptions.NotFoundException;
import ca.freshstart.exceptions.crossing.CrossingDataListException;
import ca.freshstart.data.client.repository.ClientRepository;
import ca.freshstart.data.concreteCalendarEvent.repository.ConcreteCalendarEventRepository;
import ca.freshstart.data.service.repository.ServiceRepository;
import ca.freshstart.data.session.repository.SessionRepository;
import ca.freshstart.data.availability.repository.AvTherapistDayRecordRepository;
import ca.freshstart.data.concreteEvent.repositories.ConcreteEventRepository;
import ca.freshstart.data.matching.repository.ClientMatchingConfirmationRepository;
import ca.freshstart.data.reconcile.repository.ConcreteEventReconcileRepository;
import ca.freshstart.services.EventValidationService;
import ca.freshstart.applications.matching.helpers.ConcreteEventUtils;
import ca.freshstart.helpers.CrossingDataCalculator;
import ca.freshstart.helpers.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@RestController
@RequiredArgsConstructor
public class MatchingController extends AbstractController {

    private final ClientMatchingConfirmationRepository clientMatchingConfirmationRepository;
    private final PreliminaryEventRepository preliminaryEventRepository;
    private final ClientRepository clientRepository;
    private final ServiceRepository serviceRepository;
    private final SessionRepository sessionRepository;
    private final ConcreteEventRepository concreteEventRepository;
    private final ConcreteCalendarEventRepository concreteCalendarEventRepository;
    private final ConcreteEventReconcileRepository concreteEventReconcileRepository;
    private final AvTherapistDayRecordRepository avTherapistDayRecordRepository;
    private final EventValidationService eventValidation;

    /**
     * Return all not confirmed items and confirmation for clientId
     *
     * @param sessionId id of session
     * @return Requested data
     */
    @RequestMapping(value = "/matchingBoard/session/{sessionId}/clients/mixed", method = RequestMethod.GET)
    @Secured({"ROLE_MATCHING_BOARD"})
    public Collection<ClientServiceMatchingData> getMixed(@PathVariable("sessionId") Long sessionId) {

        // get data by sessionId and group by clientId

        Map<Long, List<ClientMatchingConfirmation>> clientId2ConfirmationMap =
                clientMatchingConfirmationRepository.findBySessionId(sessionId)
                        .stream().collect(groupingBy(ClientMatchingConfirmation::getClientId));

        Map<Long, List<PreliminaryEvent>> clientId2PreEventMap =
                preliminaryEventRepository.findBySessionIdAndNullState(sessionId)
                        .stream().collect(groupingBy(PreliminaryEvent::getClientId));

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("No such session"));

        return session.getClients().stream().map(client -> {
            ClientServiceMatchingData clientServiceMatchingData = new ClientServiceMatchingData();

            Long clientId = client.getId();
            clientServiceMatchingData.setClientId(clientId);
            clientServiceMatchingData.setClientName(client.getName());

            List<PreliminaryEvent> matchingData = clientId2PreEventMap.get(clientId);
            if (matchingData == null) {
                matchingData = new ArrayList<>();
            }
            clientServiceMatchingData.setMatchingData(matchingData);

            List<ClientMatchingConfirmation> confirmationList = clientId2ConfirmationMap.get(clientId);
            if (confirmationList != null) {
                confirmationList
                        .stream()
                        .findFirst()
                        .ifPresent(clientServiceMatchingData::setConfirmationData);
            }

            return clientServiceMatchingData;
        }).collect(Collectors.toList());
    }

    /**
     * Return services if they are not send to confirmation for client in session
     *
     * @param sessionId id of session
     * @return Requested data
     */
    @RequestMapping(value = "/matchingBoard/session/{sessionId}/client/{clientId}/services", method = RequestMethod.GET)
    @Secured({"ROLE_MATCHING_BOARD"})
    public Collection<PreliminaryEvent> getPreliminaryEvents(@PathVariable("sessionId") Long sessionId,
                                                             @PathVariable("clientId") Long clientId) {

        return preliminaryEventRepository.findBySessionIdAndClientIdAndNullState(sessionId, clientId);
    }

    @RequestMapping(value = "/matchingBoard/session/{sessionId}/client/{clientId}/service/{serviceId}", method = RequestMethod.PUT)
    @Secured({"ROLE_MATCHING_BOARD"})
    public CrossingData putPreliminaryEvent(@PathVariable("sessionId") Long sessionId,
                                            @PathVariable("clientId") Long clientId,
                                            @PathVariable("serviceId") Long serviceId,
                                            @RequestParam(name = "force", required = false, defaultValue = "false") Boolean force,
                                            @RequestBody PreliminaryEvent request) {

        PreliminaryEvent clientDataItem = preliminaryEventRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("No such PreliminaryEvent"));

        if (clientDataItem.getState() != null) {
            throw new BadRequestException("Preliminary events with state Booked or Confirmation can't be changed");
        }

        if (request.getTime() == null) {
            throw new BadRequestException("ClientServiceData is not appointed for any time");
        }

        preliminaryEventRepository.findBySessionIdAndClientIdAndServiceIdAndDate(request.getSession().getId(), request.getClient().getId(), request.getService().getId(), request.getDate())
                .ifPresent(item -> {
                    if(!item.getId().equals(request.getId()))
                    throw new BadRequestException("Some PreliminaryEvent already exist at this day");
                });

        CrossingData crossingData = eventValidation.validateCrossing(request, force, sessionId);

        clientDataItem.setNote(request.getNote());
        clientDataItem.setRoom(request.getRoom());
        clientDataItem.setTherapist(request.getTherapist());
        clientDataItem.setDate(request.getDate());
        clientDataItem.setTime(request.getTime());

        preliminaryEventRepository.save(clientDataItem);

        return crossingData;
    }

    /**
     * Return list of rows of client data for session
     *
     * @param sessionId id of session
     * @return Requested data
     */
    @RequestMapping(value = "/matchingBoard/session/{sessionId}/client/{clientId}/confirmation", method = RequestMethod.GET)
    @Secured({"ROLE_MATCHING_BOARD"})
    public ClientMatchingConfirmation getConfirmation(@PathVariable("sessionId") Long sessionId,
                                                      @PathVariable("clientId") Long clientId) {

        return clientMatchingConfirmationRepository.findBySessionIdAndClientId(sessionId, clientId)
                .orElse(null);
    }

    /**
     * Create conformation. moved items to waiting confirmation state
     */
    @RequestMapping(value = "/matchingBoard/session/{sessionId}/client/{clientId}/confirmation", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK, reason = "Confirmation created")
    @Secured({"ROLE_MATCHING_BOARD"})
    public void postConfirmation(@PathVariable("sessionId") Long sessionId,
                                 @PathVariable("clientId") Long clientId,
                                 @RequestBody ClientMatchingConfirmationRequest request) {

        // validation
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("No such Session"));
        // validation
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new NotFoundException("No such Client"));

        List<PreliminaryEvent> itemsForConfirmation = preliminaryEventRepository.
                findBySessionIdAndClientIdAndNullState(sessionId, clientId);

        if (!itemsForConfirmation.isEmpty()) {

            boolean unassigned = itemsForConfirmation.stream().anyMatch(clientServiceDataItem -> clientServiceDataItem.getTime() == null);
            if (unassigned) {
                throw new BadRequestException("Some ClientServiceData is not appointed for any time");
            }

            validateCrossingForConfirmation(itemsForConfirmation, sessionId, clientId);

            //FIXME: removed while emails are switched off - in future should be uncommented
            //mailService.sendMultipleEventsCreated(itemsForConfirmation.stream().toArray(PreliminaryEvent[]::new));

            if (request.isForce()) {
                confirmPreliminaryEvents(itemsForConfirmation);
            } else {
                ClientMatchingConfirmation confirmation = clientMatchingConfirmationRepository.findBySessionIdAndClientId(sessionId, clientId)
                        .orElseGet(() -> {
                            ClientMatchingConfirmation newConfirmation;
                            newConfirmation = new ClientMatchingConfirmation();
                            newConfirmation.setClient(client);
                            newConfirmation.setSession(session);
                            newConfirmation.setSecret(UUID.randomUUID().toString());
                            return newConfirmation;
                        });

                itemsForConfirmation.forEach(confirmation::addItem);

                clientMatchingConfirmationRepository.save(confirmation);
            }
        }

    }

    private void validateCrossingForConfirmation(List<PreliminaryEvent> itemsForConfirmation, Long sessionId, Long clientId) {

        Map<Date, List<PreliminaryEvent>> date2ItemsForConfirmation = itemsForConfirmation.stream()
                .collect(groupingBy(PreliminaryEvent::getDate));

        List<CrossingData> result = new ArrayList<>();

        date2ItemsForConfirmation.forEach((Date date, List<PreliminaryEvent> events) -> {
            List<ConcreteEvent> concreteEvents = concreteEventRepository.findByDate(date);
            List<ConcreteCalendarEvent> concreteCalendarEvents = concreteCalendarEventRepository.findAllInRange(date, date);
            List<PreliminaryEvent> itemsInConfirmation = preliminaryEventRepository.
                    findBySessionIdAndClientIdAndStateAndDate(sessionId, clientId, PreliminaryEventType.confirmation, date);

            List<CrossingDataItem> crossingDataItemsForDate = events.stream()
                    .map((PreliminaryEvent event) -> {
                        Long therapistId = event.getTherapist().getId();
                        List<AvTimeRecord> timeRecords = avTherapistDayRecordRepository.findByDate(therapistId, date)
                                .map(AvTherapistDayRecord::getTimeItems)
                                .orElse(new ArrayList<>());
                        timeRecords = timeRecords.stream()
                                .filter(avTimeRecord -> avTimeRecord.getType().equals("U"))
                                .collect(toList());

                        Client client = event.getClient();
                        Map<Long, Client> id2Client = new HashMap<>();
                        if (client != null) {
                            id2Client.put(client.getId(), client);
                        }

                        CrossingData crossingData = CrossingDataCalculator.validateCrossing(event, false, sessionId, date, id2Client,
                                concreteEvents,
                                concreteCalendarEvents,
                                timeRecords,
                                itemsInConfirmation);
                        if (crossingData == null) {
                            return null;
                        } else {
                            return crossingData.getItems();
                        }
                    })
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            if (!crossingDataItemsForDate.isEmpty()) {
                CrossingData crossingData = new CrossingData();
                crossingData.setItems(crossingDataItemsForDate);
                crossingData.setSessionId(sessionId);
                crossingData.setDate(DateUtils.toDateFormat(date));
                result.add(crossingData);
            }
        });

        if (!result.isEmpty()) {
            throw new CrossingDataListException(result);
        }
    }

    /**
     * Remove confirmation. items return to nonconfirmed
     */
    @RequestMapping(value = "/matchingBoard/session/{sessionId}/client/{clientId}/confirmation", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK, reason = "Confirmation canceled")
    @Secured({"ROLE_MATCHING_BOARD"})
    @Transactional
    public void deleteConfirmation(@PathVariable("sessionId") Long sessionId,
                                   @PathVariable("clientId") Long clientId) {

        ClientMatchingConfirmation confirmation =
                clientMatchingConfirmationRepository.findBySessionIdAndClientId(sessionId, clientId)
                        .orElseThrow(() -> new NotFoundException("No such confirmation"));

        // return items to non-confirmed
        List<PreliminaryEvent> items = confirmation.getItems();
        items.forEach(item -> {
            item.setMatchingConfirmation(null);
            item.setState(null);
        });
        preliminaryEventRepository.save(items);

        clientMatchingConfirmationRepository.delete(confirmation);
    }

    /**
     * Return information about confirmation data for client
     *
     * @return Requested info
     */
    @RequestMapping(value = "/matchingBoard/confirmation/{secret}", method = RequestMethod.GET)
    public List<ClientSimpleMatchingInfo> getMatchingInfo(@PathVariable("secret") String secret) {

        List<ClientSimpleMatchingInfo> result = new ArrayList<>();

        ClientMatchingConfirmation confirmation = clientMatchingConfirmationRepository.findBySecret(secret)
                .orElseThrow(() -> new NotFoundException("No such confirmation"));

        Map<String, List<PreliminaryEvent>> date2ItemsMap = confirmation.getItems().stream()
                .collect(groupingBy(clientServiceDataItem -> DateUtils.toDateFormat(clientServiceDataItem.getDate())));

        List<Service> services = serviceRepository.findAll();

        date2ItemsMap.forEach((date, clientServiceDataItem) -> {

            ClientSimpleMatchingInfo clientSimpleMatchingInfo = new ClientSimpleMatchingInfo();
            clientSimpleMatchingInfo.setDate(date);

            List<ClientSimpleMatchingInfoItem> infoItems = clientServiceDataItem.stream().map(item -> {
                ClientSimpleMatchingInfoItem infoItem = new ClientSimpleMatchingInfoItem();

                infoItem.setTime(item.getTime());

                long serviceId = item.getServiceId();
                services.stream()
                        .filter(service -> serviceId == service.getId())
                        .findFirst()
                        .ifPresent(service -> {
                            infoItem.setService(service.getName());
                            infoItem.setDuration(service.getTime().duration());
                        });

                Room room = item.getRoom();
                if (room != null) {
                    infoItem.setRoom(room.getName());
                }

                Therapist therapist = item.getTherapist();
                if (therapist != null) {
                    infoItem.setTherapist(therapist.getName());
                }

                return infoItem;
            }).collect(Collectors.toList());

            clientSimpleMatchingInfo.setItems(infoItems);

            result.add(clientSimpleMatchingInfo);
        });

        return result;
    }

    @RequestMapping(value = "/matchingBoard/confirmation/{secret}/confirm", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK, reason = "Confirmed")
    public void confirm(@PathVariable("secret") String secret) {

        ClientMatchingConfirmation confirmation = clientMatchingConfirmationRepository.findBySecret(secret)
                .orElseThrow(() -> new NotFoundException("No such ClientMatchingConfirmation"));

        List<PreliminaryEvent> items = confirmation.removeAllItems();

        confirmPreliminaryEvents(items);

        clientMatchingConfirmationRepository.delete(confirmation);
    }

    private void confirmPreliminaryEvents(List<PreliminaryEvent> items) {
        items.forEach(item -> item.setState(PreliminaryEventType.booked));

        List<Service> services = serviceRepository.findAll();
        List<ConcreteEvent> concreteEvents = ConcreteEventUtils.generateConcreteEventsFromConfirmation(items, services);
        concreteEventRepository.save(concreteEvents);
        List<ConcreteEventReconcile> concreteEventReconciles = ConcreteEventUtils.generateConcreteEventReconcile(concreteEvents, services);
        concreteEventReconcileRepository.save(concreteEventReconciles);

        preliminaryEventRepository.save(items);
    }

    @RequestMapping(value = "/matchingBoard/confirmation/{secret}/decline", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK, reason = "Declined")
    public void decline(@PathVariable("secret") String secret) {

        ClientMatchingConfirmation confirmation = clientMatchingConfirmationRepository.findBySecret(secret)
                .orElseThrow(() -> new NotFoundException("No such ClientMatchingConfirmation"));

        // return items to non-confirmed
        List<PreliminaryEvent> items = confirmation.getItems();
        items.forEach(item -> {
            item.setMatchingConfirmation(null);
            item.setState(null);
        });
        preliminaryEventRepository.save(items);

        clientMatchingConfirmationRepository.delete(confirmation);

    }
}
