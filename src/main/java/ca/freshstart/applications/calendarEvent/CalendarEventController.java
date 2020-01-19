package ca.freshstart.applications.calendarEvent;

import ca.freshstart.applications.calendarEvent.helpers.CalendarEventUtils;
import ca.freshstart.data.calendarEvent.entity.CalendarEvent;
import ca.freshstart.data.calendarEvent.repository.CalendarEventRepository;
import ca.freshstart.data.client.entity.Client;
import ca.freshstart.data.concreteCalendarEvent.entity.ConcreteCalendarEvent;
import ca.freshstart.data.concreteCalendarEvent.repository.ConcreteCalendarEventRepository;
import ca.freshstart.data.equipment.entity.Equipment;
import ca.freshstart.data.equipment.repository.EquipmentRepository;
import ca.freshstart.data.event.repository.EventRepository;
import ca.freshstart.data.matching.types.CrossingData;
import ca.freshstart.data.room.entity.Room;
import ca.freshstart.data.room.repository.RoomRepository;
import ca.freshstart.data.therapist.entity.Therapist;
import ca.freshstart.data.therapist.repository.TherapistRepository;
import ca.freshstart.exceptions.BadRequestException;
import ca.freshstart.exceptions.ConflictException;
import ca.freshstart.exceptions.NotFoundException;
import ca.freshstart.exceptions.crossing.CrossingDataException;
import ca.freshstart.exceptions.crossing.CrossingDataListException;
import ca.freshstart.helpers.CspUtils;
import ca.freshstart.helpers.DateUtils;
import ca.freshstart.helpers.ModelValidation;
import ca.freshstart.services.EventValidationService;
import ca.freshstart.types.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ca.freshstart.applications.calendarEvent.helpers.CalendarEventUtils;

import java.lang.reflect.Array;
import java.sql.Time;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class CalendarEventController extends AbstractController {

    private final CalendarEventRepository calendarEventRepository;
    private final EquipmentRepository equipmentRepository;
    private final ConcreteCalendarEventRepository concreteCalendarEventRepository;
    private final EventRepository eventRepository;
    private final RoomRepository roomRepository;
    private final TherapistRepository therapistRepository;
    private final EventValidationService eventValidationService;
    //Service for validation data
    private final EventValidationService eventValidation;

    /**
     * Return all calendar events
     *
     * @param pageId   page to show
     * @param pageSize Item on page to show (10 by default)
     * @param sort     Field to sort by, +/- prefix defines order (ACS/DESC)
     * @return Requested calendar events
     */
    @RequestMapping(value = "/calendarEvents", method = RequestMethod.GET)
    @Secured("ROLE_EVENTS")
    public Collection<CalendarEvent> list(@RequestParam(value = "pageId", required = false, defaultValue = "1") int pageId,
                                          @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                                          @RequestParam(value = "sort", required = false) String sort) {
        Collection<CalendarEvent> list = calendarEventRepository.findAllAsList(new PageRequest(pageId - 1, pageSize, CspUtils.createSort(sort)));
        return list;
    }

    /**
     * Return count of calendar events
     *
     * @return Count of calendar events
     */
    @RequestMapping(value = "/calendarEvents/count", method = RequestMethod.GET)
    @Secured("ROLE_EVENTS")
    public Count count() {

        return new Count(calendarEventRepository.count());
    }

    /**
     * Add new calendar event
     *
     * @param newEvent calendar event to add
     * @return Id of the calendar event
     */
    @RequestMapping(value = "/calendarEvents", method = RequestMethod.POST)
    @Secured("ROLE_SETTINGS")
    @Transactional
    public IdResponse addNew(@RequestBody CalendarEvent newEvent) {

        ModelValidation.validateCalendarEventRequest(newEvent);
        newEvent.setDuration(newEvent.getEvent().getTime());

        try {
            if (!calendarEventRepository.findBy(newEvent.getName()).isEmpty()) {
                throw new DataIntegrityViolationException("");
            }
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Name should be unique and not empty");
        } finally {
            calendarEventRepository.save(newEvent);
        }

        List<ConcreteCalendarEvent> concreteCalendarEvents = CalendarEventUtils.createConcreteCalendarEvents(newEvent, newEvent.getDateStart(), newEvent.getDateEnd());
        try {
            concreteCalendarEventRepository.save(concreteCalendarEvents);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Impossible to create some concreteEvents");
        }


        return new IdResponse(newEvent.getId());
    }

    /**
     * Return information about one calendar event
     *
     * @param calendarEventId id of calendar event
     * @return Calendar event data
     */
    @RequestMapping(value = "/calendarEvents/{calendarEventId}", method = RequestMethod.GET)
    @Secured("ROLE_EVENTS")
    public CalendarEvent getById(@PathVariable("calendarEventId") Long calendarEventId) {

        return calendarEventRepository.findById(calendarEventId)
                .orElseThrow(() -> new NotFoundException("No such calendar event"));
    }

    /**
     * Update calendar event
     *
     * @param calendarEventId id of calendar event
     * @param newEvent        Updated calendar event
     */
    @RequestMapping(value = "/calendarEvents/{calendarEventId}", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK, reason = "Updated")
    @Secured("ROLE_EVENTS")
    @Transactional
    public void update(@PathVariable("calendarEventId") Long calendarEventId,
                       @RequestBody CalendarEvent newEvent) {

        ModelValidation.validateCalendarEventRequest(newEvent);

        CalendarEvent calendarEvent = calendarEventRepository.findById(calendarEventId)
                .orElseThrow(() -> new NotFoundException("No such calendar calendarEvent"));

        String newName = newEvent.getName();
        calendarEvent.setName(newName);
        calendarEvent.setCapacity(newEvent.getCapacity());

        int[] days = newEvent.getDays();

        Time originalTime = calendarEvent.getTime();
        Time newTime = newEvent.getTime();

        calendarEvent.setTime(newTime);
        calendarEvent.setDateStart(newEvent.getDateStart());
        calendarEvent.setDateEnd(newEvent.getDateEnd());
        calendarEvent.setDuration(newEvent.getEvent().getTime());

        Therapist therapist = (newEvent.getTherapist() == null)
                ? null
                : therapistRepository.findById(newEvent.getTherapist().getId())
                .orElseThrow(() -> new NotFoundException("No such therapist"));
        calendarEvent.setTherapist(therapist);

        Room room = (newEvent.getRoom() == null)
                ? null
                : roomRepository.findById(newEvent.getRoom().getId())
                .orElseThrow(() -> new NotFoundException("No such room"));
        calendarEvent.setRoom(room);

        Set<Equipment> equipmentList = null;
        Set<Equipment> newEquipment = newEvent.getEquipment();
        if (newEquipment != null && !newEquipment.isEmpty()) {
            List<Long> equipmentIds = newEquipment.stream()
                    .map(Equipment::getId)
                    .collect(Collectors.toList());
            equipmentList = equipmentRepository.findByIds(equipmentIds);
        }
        calendarEvent.setEquipment(equipmentList);

        try {
            calendarEventRepository.save(calendarEvent);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Name should be unique and not empty");
        }

        // change name for all ConcreteCalendarEvent CSP-160
        List<ConcreteCalendarEvent> concreteCalendarEvents = concreteCalendarEventRepository.findByCalendarEventId(calendarEventId);

        List<CrossingData> CrossCache = new ArrayList<CrossingData>();

        concreteCalendarEvents.forEach((ConcreteCalendarEvent concreteCalendarEvent) -> {
            // If haven't it been  changed*/
            if (!concreteCalendarEvent.isChanged()) {
                if (DateUtils.dateInDayRange(concreteCalendarEvent.getDate(), days)) {
                    concreteCalendarEvent.setName(newName);
                    concreteCalendarEvent.setTherapist(therapist);
                    concreteCalendarEvent.setRoom(room);
                    concreteCalendarEvent.setTime(newTime);
                    try {
                        eventValidationService.validateCrossing(concreteCalendarEvent, Boolean.FALSE);
                    } catch (CrossingDataException ex) {
                        CrossCache.add(ex.getCrossingData());
                    }
                } else
                    concreteCalendarEvent.setArchived(true);
            }
            //throw CrossingDataException in FRONT
            //check after assign new fields
        });

        /**
         * Create all events and filter on exists
         */
        List<ConcreteCalendarEvent> allCalendarEvents = CalendarEventUtils.createConcreteCalendarEvents(newEvent, calendarEvent.getDateStart(), calendarEvent.getDateEnd())
                .stream()
                .filter((ConcreteCalendarEvent event) -> {
                    for (ConcreteCalendarEvent compEvent : concreteCalendarEvents) {
                        if (compEvent.getDate().equals(event.getDate()))
                            return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        for (ConcreteCalendarEvent event : allCalendarEvents) {
            if (DateUtils.dateInDayRange(event.getDate(), days)) {
                event.setName(newName);
                event.setTherapist(therapist);
                event.setRoom(room);
                event.setTime(newTime);
                try {
                    eventValidationService.validateCrossing(event, Boolean.FALSE);
                } catch (CrossingDataException ex) {
                    CrossCache.add(ex.getCrossingData());
                }
            }
        }

        if (CrossCache.size() > 0) {
            //Send all conflicts at once
            throw new CrossingDataListException(CrossCache);
        }
        try {
            if (allCalendarEvents.size() > 0){
                concreteCalendarEventRepository.save(allCalendarEvents);
            }

            concreteCalendarEventRepository.save(concreteCalendarEvents);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Something went wrong with saving ConcreteCalendarEvents");
        }

    }

    /**
     * Delete calendar event
     *
     * @param calendarEventId id of calendar event
     */
    @RequestMapping(value = "/calendarEvents/{calendarEventId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK, reason = "Deleted")
    @Secured("ROLE_EVENTS")
    public void deleteById(@PathVariable("calendarEventId") Long calendarEventId) {

        CalendarEvent calendarEvent = calendarEventRepository.findById(calendarEventId)
                .orElseThrow(() -> new NotFoundException("No such calendar event"));

        calendarEvent.setArchived(true);

        calendarEventRepository.save(calendarEvent);

//        Date today = new Date();
//        Time now = new Time(today.getTime() % Constants.MILLISECONDS_PER_DAY);
//
//        List<ConcreteCalendarEvent> concreteCalendarEventsAfter = concreteCalendarEventRepository.findByCalendarEventIdAfterDate(calendarEventId, today, now);
//        List<ConcreteCalendarEvent> toRemove = concreteCalendarEventsAfter.stream()
//                .filter((ConcreteCalendarEvent concreteCalendarEvent) -> {
//                    // CSP-163 remove events which have not clients
//                    Set<Client> clients = concreteCalendarEvent.getClients();
//                    return clients == null || clients.isEmpty();
//                })
//                .collect(Collectors.toList());
//        concreteCalendarEventRepository.delete(toRemove);
        List<ConcreteCalendarEvent> concreteCalendarEvents = concreteCalendarEventRepository.findByCalendarEventId(calendarEventId)
                .stream()
                .map((ConcreteCalendarEvent event) -> {
                    event.setArchived(true);
                    return event;
                })
                .collect(Collectors.toList());

        concreteCalendarEventRepository.save(concreteCalendarEvents);

    }

    // -----------------------------------

    /**
     * Return calendar events without therapists that are near to come
     *
     * @param pageId   page to show
     * @param pageSize Item on page to show (10 by default)
     * @param sort     Field to sort by, +/- prefix defines order (ACS/DESC)
     * @return Requested calendar events
     */
    @RequestMapping(value = "/calendarEvents/urgent", method = RequestMethod.GET)
    @Secured("ROLE_EVENTS")
    public Collection<CalendarEvent> urgentEvents(@RequestParam(value = "pageId", required = false, defaultValue = "1") int pageId,
                                                  @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                                                  @RequestParam(value = "sort", required = false) String sort) {

        return calendarEventRepository.findAllUrgent(new PageRequest(pageId - 1, pageSize, CspUtils.createSort(sort)));
    }

    /**
     * Return count of calendar events
     *
     * @return Count of calendar events
     */
    @RequestMapping(value = "/calendarEvents/urgent/count", method = RequestMethod.GET)
    @Secured("ROLE_EVENTS")
    public Count countUrgent() {

        return new Count(calendarEventRepository.countUrgent());
    }

    /**
     * Update therapist of calendar event
     *
     * @param calendarEventId    id of calendar event
     * @param therapistIdRequest linked id of therapist
     */
    @RequestMapping(value = "/calendarEvents/{calendarEventId}/therapist", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK, reason = "Update done")
    @Secured("ROLE_EVENTS")
    @Transactional
    public void updateEventTherapist(@PathVariable("calendarEventId") Long calendarEventId,
                                     @RequestBody IdRequest therapistIdRequest) {

        CalendarEvent calendarEvent = calendarEventRepository.findById(calendarEventId)
                .orElseThrow(() -> new NotFoundException("No such calendar event"));

        List<ConcreteCalendarEvent> concreteEvents = concreteCalendarEventRepository.findByCalendarEventId(calendarEventId);

        Long therapistId = therapistIdRequest.getId();
        Therapist therapist;
        if (therapistId != null) {
            therapist = therapistRepository.findById(therapistId)
                    .orElseThrow(() -> new NotFoundException("No such therapist"));
        } else {
            therapist = null;
        }

        calendarEvent.setTherapist(therapist);
        calendarEventRepository.save(calendarEvent);

        concreteEvents.forEach(eventConcrete -> eventConcrete.setTherapist(therapist));
        concreteCalendarEventRepository.save(concreteEvents);
    }

    /**
     * Update room of calendar event
     *
     * @param calendarEventId id of calendar event
     * @param roomIdRequest   linked id of room
     */
    @RequestMapping(value = "/calendarEvents/{calendarEventId}/room", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK, reason = "Update done")
    @Secured("ROLE_EVENTS")
    @Transactional
    public void updateEventRoom(@PathVariable("calendarEventId") Long calendarEventId,
                                @RequestBody IdRequest roomIdRequest) {

        CalendarEvent calendarEvent = calendarEventRepository.findById(calendarEventId)
                .orElseThrow(() -> new NotFoundException("No such calendar event"));

        List<ConcreteCalendarEvent> concreteEvents = concreteCalendarEventRepository.findByCalendarEventId(calendarEventId);

        Long roomId = roomIdRequest.getId();
        Room room;
        if (roomId != null) {
            room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new NotFoundException("No such room"));
        } else {
            room = null;
        }

        calendarEvent.setRoom(room);
        calendarEventRepository.save(calendarEvent);

        concreteEvents.forEach(eventConcrete -> eventConcrete.setRoom(room));
        concreteCalendarEventRepository.save(concreteEvents);
    }

    /**
     * Update equipments of calendar event
     *
     * @param calendarEventId id of calendar event
     * @param equipmentIds    linked id of equipments
     */
    @RequestMapping(value = "/calendarEvents/{calendarEventId}/equipment", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK, reason = "Update done")
    @Secured("ROLE_EVENTS")
    @Transactional
    public void updateEventEquipment(@PathVariable("calendarEventId") Long calendarEventId,
                                     @RequestBody Long[] equipmentIds) {

        CalendarEvent calendarEvent = calendarEventRepository.findById(calendarEventId)
                .orElseThrow(() -> new NotFoundException("No such calendar event"));

        Set<Equipment> equipments;
        if (equipmentIds != null && equipmentIds.length > 0) {
            equipments = equipmentRepository.findByIds(Arrays.asList(equipmentIds));

            if (equipments == null || equipments.size() < 1) {
                throw new NotFoundException("No such equipments");
            }
        } else {
            equipments = null;
        }

        calendarEvent.setEquipment(equipments);
        calendarEventRepository.save(calendarEvent);
    }
}
