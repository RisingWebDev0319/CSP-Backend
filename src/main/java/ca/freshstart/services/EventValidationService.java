package ca.freshstart.services;

import ca.freshstart.data.availability.entity.AvTherapistDayRecord;
import ca.freshstart.data.availability.entity.AvTimeRecord;
import ca.freshstart.data.availability.repository.AvTherapistDayRecordRepository;
import ca.freshstart.data.calendarEvent.repository.CalendarEventRepository;
import ca.freshstart.data.client.entity.Client;
import ca.freshstart.data.client.repository.ClientRepository;
import ca.freshstart.data.concreteCalendarEvent.entity.ConcreteCalendarEvent;
import ca.freshstart.data.concreteCalendarEvent.repository.ConcreteCalendarEventRepository;
import ca.freshstart.data.concreteEvent.entity.ConcreteEvent;
import ca.freshstart.data.concreteEvent.interfaces.BaseCrossEvent;
import ca.freshstart.data.concreteEvent.repositories.ConcreteEventRepository;
import ca.freshstart.data.matching.types.CrossingData;
import ca.freshstart.data.matching.types.PreliminaryEventType;
import ca.freshstart.data.suggestions.entity.PreliminaryEvent;
import ca.freshstart.data.suggestions.repository.PreliminaryEventRepository;
import ca.freshstart.data.therapist.repository.TherapistRepository;
import ca.freshstart.exceptions.BadRequestException;
import ca.freshstart.helpers.CrossingDataCalculator;
import ca.freshstart.helpers.DateUtils;
import ca.freshstart.types.EntityId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service("ConcreteEvent")
public class EventValidationService {

    @Autowired
    private ConcreteCalendarEventRepository concreteCalendarEventRepository;
    @Autowired
    private CalendarEventRepository calendarEventRepository;
    @Autowired
    private TherapistRepository therapistRepository;
    @Autowired
    private ConcreteEventRepository concreteEventRepository;
    @Autowired
    private AvTherapistDayRecordRepository avTherapistDayRecordRepository;
    @Autowired
    private PreliminaryEventRepository preliminaryEventRepository;
    @Autowired
    private ClientRepository clientRepository;
    //Data

    private Date date;
    private Set<Date> dates;
    private List<ConcreteEvent> concreteEvents;
    private List<ConcreteCalendarEvent> concreteCalendarEvents;
    private Map<Long, Client> id2Client;
    private Long therapistId;
    private List<AvTimeRecord> timeRecords;
    private List<PreliminaryEvent> preliminaryEvents;

    private void initValidationService(BaseCrossEvent event) {
        this.date = getDate(event);
        this.dates = getDates(date);
        this.concreteEvents = getConcreteEvents(this.dates);
        this.concreteCalendarEvents = getConcreteCalendarEvents(date);
        this.id2Client = new HashMap<>();
        this.therapistId = getTherapistId(event);
        this.timeRecords = getTimeRecords(this.therapistId, this.date);
        this.preliminaryEvents = getPreliminaryEvents(dates);
    }

    public CrossingData validateCrossing(PreliminaryEvent event, Boolean force, Long sessionId) {

        initValidationService(event);

        this.preliminaryEvents = filterSelf(event, preliminaryEventRepository.findByStateInDates(PreliminaryEventType.confirmation, dates));
        Client client = event.getClient();
        if (client != null) {
            this.id2Client.put(client.getId(), client);
        }

        return validate(event, force, sessionId);
    }

    public void validatePast(Date date, String message) {
        Date today = DateUtils.getDayStart(new Date());
        Date startOfDate = DateUtils.getDayStart(date);

        if (startOfDate.getTime() >= today.getTime()) {
            throw new BadRequestException(message);
        }
    }

    public CrossingData validateCrossing(ConcreteEvent event, Boolean force) {

        initValidationService(event);

        this.concreteEvents = filterSelf(event, this.concreteEvents);
        Client client = event.getClient();
        if (client != null) {
            this.id2Client.put(client.getId(), client);
        }

        return validate(event, force, null);
    }

    public CrossingData validateCrossing(ConcreteCalendarEvent event, Boolean force) {

        initValidationService(event);
        this.concreteCalendarEvents = filterSelf(event, this.concreteCalendarEvents);

        Set<Client> clients = event.getClients();
        if (clients.size() > 0) {
            for (Client client : clients) {
                this.id2Client.put(client.getId(), client);
            }
        }

        return validate(event, force, null);
    }

    private CrossingData validate(BaseCrossEvent event, Boolean force, Long sessionId) {
        return CrossingDataCalculator.validateCrossing(event, force, sessionId, this.date, id2Client,
                this.concreteEvents,
                this.concreteCalendarEvents,
                this.timeRecords,
                this.preliminaryEvents
        );
    }

    private Long getTherapistId(BaseCrossEvent event) {
        return event.getTherapist() != null ? event.getTherapist().getId() : null;
    }

    private Date getDate(BaseCrossEvent event) {
        return event.getDate();
    }

    private Set<Date> getDates(Date date) {
        Set<Date> dates = new HashSet<>();
        dates.add(date);
        return dates;
    }

    private List<ConcreteEvent> getConcreteEvents(Set<Date> dates) {
        return concreteEventRepository.findInDates(dates);
    }

    private List<ConcreteCalendarEvent> getConcreteCalendarEvents(Date date) {
        return concreteCalendarEventRepository.findAllInRange(date, date);
    }

    private List<AvTimeRecord> getTimeRecords(Long therapistId, Date date) {
        return avTherapistDayRecordRepository.findByDate(therapistId, date)
                .map(AvTherapistDayRecord::getTimeItems)
                .orElse(new ArrayList<>())
                .stream()
                .filter(avTimeRecord -> avTimeRecord.getType().equals("U"))
                .collect(toList());
    }

    private List<PreliminaryEvent> getPreliminaryEvents(Set<Date> dates) {
        return preliminaryEventRepository.findByStateInDates(PreliminaryEventType.confirmation, dates);
    }

    private List<PreliminaryEvent> filterSelf(PreliminaryEvent intersectingItem, List<PreliminaryEvent> intersectedItems) {
        return intersectedItems
                .stream()
                .filter(preliminaryEvent -> !preliminaryEvent.getId().equals(intersectingItem.getId()))
                .collect(Collectors.toList());
    }

    private <T extends EntityId> List<T> filterSelf(T intersectingItem, List<T> intersectedItems) {
        Long intersectingItemId = intersectingItem.getId();
        return intersectedItems.stream()
                .filter(item -> !item.getId().equals(intersectingItemId))
                .collect(toList());
    }

}
