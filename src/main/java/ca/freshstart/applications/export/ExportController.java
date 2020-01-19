package ca.freshstart.applications.export;

import ca.freshstart.data.client.entity.Client;
import ca.freshstart.data.client.repository.ClientRepository;
import ca.freshstart.data.concreteCalendarEvent.entity.ConcreteCalendarEvent;
import ca.freshstart.data.concreteCalendarEvent.repository.ConcreteCalendarEventRepository;
import ca.freshstart.data.concreteEvent.entity.ConcreteEvent;
import ca.freshstart.data.concreteEvent.repositories.ConcreteEventRepository;
import ca.freshstart.data.eventTypes.entity.EventTypes;
import ca.freshstart.data.service.entity.Service;
import ca.freshstart.exceptions.BadRequestException;
import ca.freshstart.exceptions.NotFoundException;
import ca.freshstart.exceptions.UnathorizedException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.sql.Time;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
class ExportedConcreteEvent {
    String serviceName;
    String serviceDescription;
    Time time;         // Service startTime
    Float cost;      //Service cost
    String therapist;  //therapist Name
    Date date;
    boolean isEvent;    //EventType
}

@Data
class ExportedTherapist {
    Long id;
    String name;
}

@Data
class ExportedClient {
    Long id;
    String name;
}

@Data
class ExportedRoom {
    Long id;
    String name;
}

@Data
class ExportedAppointments {
    String date;
    String time;
    String serviceName;
    String serviceDescription;
    Float salePrice;
    Float purchasePrice;
    String therapist;
    String room;
    String note;
    String clients;
    boolean isEvent;
    Integer duration;
}

@RestController
@RequiredArgsConstructor
public class ExportController {

    private static final String API_KEY = "Gp3wM2PW3Fvt92fcxTzfvDPtTxfyHxdatWS8aVq5";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM-dd-yyyy");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("hh:mm aaa");

    @Autowired
    private final ConcreteCalendarEventRepository concreteCalendarEventRepository;
    private final ConcreteEventRepository concreteEventRepository;
    private final ClientRepository clientRepository;


    @RequestMapping(value = "/export/therapists", method = RequestMethod.GET)
    public List<ExportedTherapist> getTherpists(
            @RequestParam(value = "start_date", required = true) @DateTimeFormat(pattern = "dd-MM-yyyy") Date startDate,
            @RequestParam(value = "end_date", required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") Date endDate,
            @RequestHeader(value = "APIKEY", required = true) String ApiKey
    ) {
        // check apikey
        if (!API_KEY.equals(ApiKey)) {
            throw new UnathorizedException("Invalid access key.");
        }
        // check enddate
        if (endDate == null) {
            endDate = startDate;
        }
        if (endDate.before(startDate)) {
            throw new BadRequestException("Invalid end date.");
        }
        HashSet<ExportedTherapist> export = new HashSet<>();

        export.addAll(concreteCalendarEventRepository.findAllInRange(startDate, endDate)
                .stream()
                .filter( event -> event.getTherapist() != null)
                .map(
                        (event) -> {
                            ExportedTherapist therapist = new ExportedTherapist();
                            therapist.id = event.getTherapist().getId();
                            therapist.name = event.getTherapist().getName();
                            return therapist;
                        }
                ).distinct().collect(Collectors.toList()));
        export.addAll(concreteEventRepository.findByDateInRange(startDate, endDate)
                .stream()
                .filter( event -> event.getTherapist() != null)
                .map(
                        (event) -> {
                            ExportedTherapist therapist = new ExportedTherapist();
                            therapist.id = event.getTherapist().getId();
                            therapist.name = event.getTherapist().getName();
                            return therapist;
                        }
                ).distinct().collect(Collectors.toList()));

        return new ArrayList<>(export);
    }


    @RequestMapping(value = "/export/rooms", method = RequestMethod.GET)
    public List<ExportedRoom> getRooms(
            @RequestParam(value = "start_date", required = true) @DateTimeFormat(pattern = "dd-MM-yyyy") Date startDate,
            @RequestParam(value = "end_date", required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") Date endDate,
            @RequestHeader(value = "APIKEY", required = true) String ApiKey
    ) {
        // check apikey
        if (!API_KEY.equals(ApiKey)) {
            throw new UnathorizedException("Invalid access key.");
        }
        // check enddate
        if (endDate == null) {
            endDate = startDate;
        }
        if (endDate.before(startDate)) {
            throw new BadRequestException("Invalid end date.");
        }
        HashSet<ExportedRoom> export = new HashSet<>();

        export.addAll(concreteCalendarEventRepository.findAllInRange(startDate, endDate)
                .stream()
                .filter( event -> event.getRoom() != null)
                .map(
                        (event) -> {
                            ExportedRoom room = new ExportedRoom();
                            room.id = event.getRoom().getId();
                            room.name = event.getRoom().getName();
                            return room;
                        }
                ).distinct().collect(Collectors.toList()));
        export.addAll(concreteEventRepository.findByDateInRange(startDate, endDate)
                .stream()
                .filter( event -> event.getRoom() != null)
                .map(
                        (event) -> {
                            ExportedRoom room = new ExportedRoom();
                            room.id = event.getRoom().getId();
                            room.name = event.getRoom().getName();
                            return room;
                        }
                ).distinct().collect(Collectors.toList()));

        return new ArrayList<>(export);
    }


    @RequestMapping(value = "/export/clients", method = RequestMethod.GET)
    public List<ExportedClient> getClients(
            @RequestParam(value = "start_date", required = true) @DateTimeFormat(pattern = "dd-MM-yyyy") Date startDate,
            @RequestParam(value = "end_date", required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") Date endDate,
            @RequestHeader(value = "APIKEY", required = true) String ApiKey
    ) {
        // check apikey
        if (!API_KEY.equals(ApiKey)) {
            throw new UnathorizedException("Invalid access key.");
        }
        // check enddate
        if (endDate == null) {
            endDate = startDate;
        }
        if (endDate.before(startDate)) {
            throw new BadRequestException("Invalid end date.");
        }
        HashSet<ExportedClient> export = new HashSet<>();

        export.addAll(concreteCalendarEventRepository.findAllInRange(startDate, endDate)
                .stream()
                .filter( event -> event.getClients() != null)
                .map(
                        (event) -> {
                            Set<Client> clients = event.getClients();
                            List<ExportedClient> exportedClients = new ArrayList<>();
                            for (Client client : clients) {
                                ExportedClient exportedClient = new ExportedClient();
                                exportedClient.id = client.getId();
                                exportedClient.name = client.getReadbleName();
                                exportedClients.add(exportedClient);
                            }
                            return exportedClients;
                        }
                )
                .flatMap(
                        list -> list.stream()
                ).distinct().collect(Collectors.toList()));


        export.addAll(concreteEventRepository.findByDateInRange(startDate, endDate)
                .stream()
                .filter( event -> event.getClient() != null)
                .map(
                        (event) -> {
                            Client client = event.getClient();
                            ExportedClient exportedClient = new ExportedClient();
                            exportedClient.id = client.getId();
                            exportedClient.name = client.getReadbleName();
                            return exportedClient;
                        }
                ).distinct().collect(Collectors.toList()));

        return new ArrayList<>(export);
    }


    @RequestMapping(value = "/export/appointments", method = RequestMethod.GET)
    public List<ExportedAppointments> getAppointments(
            @RequestParam(value = "start_date", required = true) @DateTimeFormat(pattern = "dd-MM-yyyy") Date startDate,
            @RequestParam(value = "end_date", required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") Date endDate,
            @RequestParam(value = "therapistId", required = false) Long therapistId,
            @RequestParam(value = "clientId", required = false) Long clientId,
            @RequestParam(value = "roomId", required = false) Long roomId,
            @RequestHeader(value = "APIKEY", required = true) String ApiKey
    ) {
        if (!API_KEY.equals(ApiKey)) {
            throw new UnathorizedException("Invalid access key.");
        }
        // check enddate
        if (endDate == null) {
            endDate = startDate;
        }
        if (endDate.before(startDate)) {
            throw new BadRequestException("Invalid end date.");
        }

        ArrayList<ExportedAppointments> export = new ArrayList<>();

        Stream<ConcreteCalendarEvent> calendarStream = concreteCalendarEventRepository.findAllInRange(startDate, endDate)
                .stream()
                .filter( event -> event.getTherapist() != null);

        if(therapistId != null) {
            calendarStream = calendarStream.filter(
                    event ->  therapistId.equals(event.getTherapist().getId())
            );
        }
        if(clientId != null) {
            calendarStream = calendarStream
                    .filter( event -> event.getClientsIds() != null)
                    .filter(
                    event ->  event.getClientsIds().contains(clientId)
            );
        }
        if(roomId != null) {
            calendarStream = calendarStream
                    .filter( event -> event.getRoom() != null)
                    .filter(
                    event -> roomId.equals(event.getRoom().getId())
            );
        }

        export.addAll(calendarStream.map(
                event -> {
                    ExportedAppointments appointment = new ExportedAppointments();
                    appointment.date = DATE_FORMAT.format(event.getDate());
                    appointment.time = TIME_FORMAT.format(event.getTime());
                    appointment.serviceName = event.getCalendarEvent().getEventName();
                    appointment.serviceDescription = event.getCalendarEvent().getDescription();
                    appointment.salePrice = event.getSalePrice();
                    appointment.purchasePrice = event.getPurchasePrice();
                    if(event.getRoom() != null) {
                        appointment.room = event.getRoom().getName();
                    } else {
                        appointment.room = "";
                    }
                    appointment.note = null;
                    appointment.therapist = event.getTherapist().getName();
                    appointment.isEvent = true;
                    appointment.duration = event.getDuration().getProcessing();
                    List<String> clients = event.getClients().stream().map(
                            client -> client.getReadbleName()
                    ).collect(Collectors.toList());
                    appointment.clients = String.join(", ", clients);
                    return appointment;
                }
        ).collect(Collectors.toList()));

        Stream<ConcreteEvent> concreteEventStream = concreteEventRepository.findByDateInRange(startDate, endDate)
                .stream()
                .filter( event -> event.getTherapist() != null)
                .filter( event -> event.getRoom() != null)
                .filter( event -> event.getClient() != null);

        if(therapistId != null) {
            concreteEventStream = concreteEventStream.filter(
                    event ->  therapistId.equals(event.getTherapist().getId())
            );
        }
        if(clientId != null) {
            concreteEventStream = concreteEventStream.filter(
                    event ->  clientId.equals(event.getClient().getId())
            );
        }
        if(roomId != null) {
            concreteEventStream = concreteEventStream.filter(
                    event -> roomId.equals(event.getRoom().getId())
            );
        }

        export.addAll(concreteEventStream.map(
                event -> {
                    Service service = event.getService();
                    ExportedAppointments appointment = new ExportedAppointments();
                    appointment.date = DATE_FORMAT.format(event.getDate());
                    appointment.time = TIME_FORMAT.format(event.getTime());
                    appointment.serviceName = (service == null) ? "" : service.getName();
                    appointment.serviceDescription = (service == null) ? "" : service.getBillingDescription();
                    appointment.salePrice = event.getSalePrice();
                    appointment.purchasePrice = event.getPurchasePrice();
                    appointment.room = event.getRoom().getName();
                    appointment.note = event.getNote();
                    appointment.therapist = event.getTherapist().getName();
                    appointment.isEvent = false;
                    appointment.duration = service.getTime().getProcessing();
                    appointment.clients = event.getClient().getName();
                    return appointment;
                }
        ).collect(Collectors.toList()));

        return export;
    }




    @RequestMapping(value = "/export/appointment", method = RequestMethod.GET)
    public List<ExportedConcreteEvent> getListLegacy(@RequestHeader(value = "start_date") @DateTimeFormat(pattern = "dd-MM-yyyy") Date startDate,
                                                     @RequestHeader(value = "end_date") @DateTimeFormat(pattern = "dd-MM-yyyy") Date endDate,
                                                     @RequestHeader(value = "client", required = true) String clientEmail,
                                                     @RequestHeader(value = "APIKEY", required = true) String ApiKey) {

        if (!ApiKey.equals("accesskey")) {
            throw new NotFoundException("Invalid access key.");
        }

        Client client = clientRepository.findClientByEmail(clientEmail);
        if (client == null) {
            throw new NotFoundException("No client with such email.");
        }

        List<ExportedConcreteEvent> export = new ArrayList<>();

        List<ConcreteCalendarEvent> concreteCalendarEventList = concreteCalendarEventRepository.findAllInRange(startDate, endDate)
                .stream()
                .filter((ConcreteCalendarEvent event) -> {
                    Set<Client> clients = event.getClients();
                    boolean contains = clients.contains(client);
                    if (contains) {
                        ExportedConcreteEvent eEvent = new ExportedConcreteEvent();
                        eEvent.serviceName = event.getCalendarEvent().getEventName();
                        eEvent.serviceDescription = event.getCalendarEvent().getDescription();
                        eEvent.cost = event.getSalePrice();
                        eEvent.date = event.getDate();
                        eEvent.time = event.getTime();
                        eEvent.therapist = event.getTherapist().getName();
                        EventTypes eventType = event.getEvent().getEventType();
                        eEvent.isEvent = true;
                        export.add(eEvent);
                    }

                    return contains;
                })
                .collect(Collectors.toList());

        List<ConcreteEvent> concreteEventList = concreteEventRepository.findByDateInRange(startDate, endDate)
                .stream()
                .filter((ConcreteEvent event) -> {
                    if (event.getClient() != null
                            && (event.getClient().getId() == client.getId())) {

                        Service service = event.getService();
                        ExportedConcreteEvent eEvent = new ExportedConcreteEvent();
                        eEvent.serviceName = (service == null) ? "" : service.getName();
                        eEvent.serviceDescription = (service == null) ? "" : service.getBillingDescription();
                        eEvent.cost = event.getSalePrice();
                        eEvent.date = event.getDate();
                        eEvent.time = event.getTime();
                        eEvent.therapist = event.getTherapist().getName();
                        eEvent.isEvent = false;
                        export.add(eEvent);

                        return true;
                    }
                    return false;
                }).collect(Collectors.toList());

        return export;
    }
}
