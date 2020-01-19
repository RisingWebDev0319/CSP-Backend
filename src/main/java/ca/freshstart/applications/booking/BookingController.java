package ca.freshstart.applications.booking;


import ca.freshstart.applications.booking.interfaces.WsService;
import ca.freshstart.applications.booking.types.BookingEventNotification;
import ca.freshstart.data.appUser.entity.AppUser;
import ca.freshstart.data.client.entity.Client;
import ca.freshstart.data.client.repository.ClientRepository;
import ca.freshstart.data.concreteCalendarEvent.entity.ConcreteCalendarEvent;
import ca.freshstart.data.concreteCalendarEvent.repository.ConcreteCalendarEventRepository;
import ca.freshstart.data.concreteEvent.entity.ConcreteEvent;
import ca.freshstart.data.serviceCategory.entity.ServiceCategory;
import ca.freshstart.data.week.entity.Week;
import ca.freshstart.data.concreteEvent.entity.ConcreteEventChange;
import ca.freshstart.data.concreteEvent.entity.ConcreteEventChangeValue;
import ca.freshstart.data.concreteEvent.entity.ConcreteEventSubStatus;
import ca.freshstart.data.concreteEvent.repositories.ConcreteEventChangeCodeRepository;
import ca.freshstart.data.concreteEvent.repositories.ConcreteEventRepository;
import ca.freshstart.data.concreteEvent.repositories.ConcreteEventSubStatusRepository;
import ca.freshstart.data.concreteEvent.types.ConcreteEventState;
import ca.freshstart.data.concreteEvent.types.CrossingItem;
import ca.freshstart.data.event.entity.Event;
import ca.freshstart.data.matching.types.CrossingData;
import ca.freshstart.data.reconcile.entity.ConcreteEventReconcile;
import ca.freshstart.data.reconcile.repository.ConcreteEventReconcileRepository;
import ca.freshstart.data.room.entity.Room;
import ca.freshstart.data.service.entity.Service;
import ca.freshstart.data.service.repository.ServiceRepository;
import ca.freshstart.data.session.entity.Session;
import ca.freshstart.data.session.repository.SessionRepository;
import ca.freshstart.data.suggestions.repository.PreliminaryEventRepository;
import ca.freshstart.data.therapist.entity.Therapist;
import ca.freshstart.data.week.repository.WeekRepository;
import ca.freshstart.data.therapist.repository.TherapistRepository;
import ca.freshstart.exceptions.BadRequestException;
import ca.freshstart.exceptions.ConflictException;
import ca.freshstart.exceptions.NotFoundException;
import ca.freshstart.helpers.DateUtils;
import ca.freshstart.helpers.TaskManager;
import ca.freshstart.services.EventValidationService;
import ca.freshstart.types.*;
import ca.freshstart.helpers.CspUtils;
import static ca.freshstart.helpers.CspUtils.isNullOrEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.sql.Time;
import java.util.*;
import java.util.stream.Collectors;

import static ca.freshstart.helpers.ModelValidation.validateServiceCategory;
import static ca.freshstart.helpers.ModelValidation.validateWeek;

@RestController
@RequiredArgsConstructor
public class BookingController extends AbstractController {

    private final ConcreteEventRepository concreteEventRepository;
    private final ConcreteCalendarEventRepository concreteCalendarEventRepository;
    private final ConcreteEventReconcileRepository concreteEventReconcileRepository;
    private final PreliminaryEventRepository preliminaryEventRepository;
    private final ConcreteEventSubStatusRepository concreteEventSubStatusRepository;
    private final WeekRepository weekRepository;
    private final TaskManager taskManager;
    private final ServiceRepository serviceRepository;
    private final ConcreteEventChangeCodeRepository concreteEventChangeCodeRepository;
    private final TherapistRepository therapistRepository;
    private final ClientRepository clientRepository;
    private final SessionRepository sessionRepository;
    private final WsService wsService;
    private final EventValidationService eventValidation;




     /**
     * Return list of events
     *
     * @param pageId   page to show
     * @param pageSize Item on page to show (10 by default)
     * @param sort     Field to sort by, +/- prefix defines order (ACS/DESC)
     * @return Requested events
     */
    @RequestMapping(value = "/booking/week", method = RequestMethod.GET)
    @Secured("ROLE_BOOKING")
    public Collection<Week> getAll(@RequestParam(value = "pageId", required = false, defaultValue = "1") int pageId,
                                      @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                                      @RequestParam(value = "sort", required = false) String sort) {
        return weekRepository.findAll();
    } //findAllAsList(new PageRequest(pageId - 1, pageSize, CspUtils.createSort(sort)));


    /**
     * Add new serviceCategory
     *
     * @param week week to add.
     * @return Id
     */
    @RequestMapping(value = "/week", method = RequestMethod.POST)
    @Secured("ROLE_BOOKING")
    public IdResponse addNew(@RequestBody Week week) {

        validateWeek(week);

        if (week.getName().length() > 255) {
            throw new BadRequestException("Therapist Week name is too long");
        }

        try {
            week = weekRepository.save(week);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Such week already exists");
        }

        return new IdResponse(week.getId());
    }


    /**
     * Delete week
     *
     * @param weekId id of serviceCategory
     */
    @RequestMapping(value = "/week/{weekId}", method = RequestMethod.DELETE)
    @Secured("ROLE_BOOKING")
    public Collection<Therapist> delete(@PathVariable("weekId") Long weekId,
                                        HttpServletResponse response) {

        Week week = weekRepository.findById(weekId)
                .orElseThrow(() -> new NotFoundException("No such week"));

//        if (!isNullOrEmpty(week.getTherapists())) {
//            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//            return week.getTherapists();
//        }

        weekRepository.delete(week);

        return null;
    }


    /**
     * Return information about one serviceCategory
     *
     * @param weekId id of week
     * @return week data
     */
    @RequestMapping(value = "/week/{weekId}", method = RequestMethod.GET)
    @Secured("ROLE_BOOKING")
    public Week getById(@PathVariable("weekId") Long weekId) {

        return weekRepository.findById(weekId)
                .orElseThrow(() -> new NotFoundException("No such Therapist Week"));
    }


    /**
     * Update week
     *
     * @param weekId   id of week
     * @param weekFrom Updated week
     */
    @RequestMapping(value = "/week/{weekId}", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK, reason = "Updated")
    @Secured("ROLE_BOOKING")
    public void update(@PathVariable("weekId") Long weekId,
                       @RequestBody Week weekFrom) {

        validateWeek(weekFrom);

        Week week = weekRepository.findById(weekId)
                .orElseThrow(() -> new NotFoundException("No such week"));

        week.setName(weekFrom.getName());

        try {
            weekRepository.save(week);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Such week already exists");
        }
    }

    /**
     * Override services assigned to category
     *
     * @param  weekId id of week
     * @param  therapists  array of therapists ids
     */
    @RequestMapping(value = "/week/{weekId}/therapists", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK, reason = "Updated")
    @Secured("ROLE_BOOKING")
    public void addNewWeek(@PathVariable("weekId") Long weekId,
                              @RequestBody long[] therapists) {

        Week week = weekRepository.findById(weekId)
                .orElseThrow(() -> new NotFoundException("No such Therapist Week"));

        List<Long> ids = Arrays.stream(therapists).boxed().collect(Collectors.toList());
        if (!ids.isEmpty()) {
            List<Therapist> therapistList = therapistRepository.findByIds(ids);
            week.setTherapists(new HashSet<>(therapistList));
        } else {
            week.setTherapists(new HashSet<>());
        }

        weekRepository.save(week);
    }


    /**
     * return all event for date
     *
     * @param date
     * @return
     */
    @RequestMapping(value = "/booking/events", method = RequestMethod.GET)
    @Secured("ROLE_BOOKING")
    public Collection<ConcreteEvent> getEvents(@RequestParam(value = "date", required = false) @DateTimeFormat(pattern = Constants.DATE_FORMAT) Date date,
                                               @RequestParam(value = "dateStart", required = false) @DateTimeFormat(pattern = Constants.DATE_FORMAT) Date dateStart,
                                               @RequestParam(value = "dateEnd", required = false) @DateTimeFormat(pattern = Constants.DATE_FORMAT) Date dateEnd) {
        if (date != null)
            return concreteEventRepository.findByDate(date);
        else if (dateStart != null && dateEnd != null)
            return concreteEventRepository.findByDateInRange(dateStart, dateEnd);
        else
            throw new BadRequestException("Request should have date parameter or pair dateStart & dateEnd");
    }

    @RequestMapping(value = "/booking/events/mine", method = RequestMethod.GET)
    @Secured("ROLE_THERAPIST")
    public Collection<ConcreteEvent> getMyEvents(@RequestParam("dateStart") @DateTimeFormat(pattern = Constants.DATE_FORMAT) Date dateStart,
                                                 @RequestParam("dateEnd") @DateTimeFormat(pattern = Constants.DATE_FORMAT) Date dateEnd) {
        AppUser user = getCurrentUser();

        Therapist therapist = therapistRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new BadRequestException("User is not a therapist"));

        if (dateStart != null && dateEnd != null)
            return concreteEventRepository.findByTherapistAndDateInRange(therapist.getId(), dateStart, dateEnd);
        else
            throw new BadRequestException("Request should have pair dateStart & dateEnd");
    }

    /**
     * return info about one concrete event item
     *
     * @param eventId
     * @return
     */
    @RequestMapping(value = "/booking/event/{eventId}", method = RequestMethod.GET)
    @Secured("ROLE_BOOKING")
    public ConcreteEvent getEvent(@PathVariable("eventId") Long eventId) {
        return concreteEventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("No such ConcreteEvent"));
    }

    /**
     * update event. only non null fileds will be changed in event. null fileds will preserv value
     *
     * @param id
     * @param force         - update time/date event if there are intersections
     * @param noEmails      - if true no emails will be send to therapist
     * @param concreteEvent
     */
    @RequestMapping(value = "/booking/event/{eventId}", method = RequestMethod.PUT)
    @Secured("ROLE_BOOKING")
    public CrossingData editEventFromBooking(@PathVariable("eventId") Long id,
                                             @RequestParam(name = "force", required = false, defaultValue = "false") Boolean force,
                                             @RequestParam(name = "noEmails", required = false, defaultValue = "false") Boolean noEmails,
                                             @RequestBody ConcreteEvent concreteEvent) {

        ConcreteEvent foundEvent = concreteEventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No such ConcreteEvent"));

        CrossingData crossingData = eventValidation.validateCrossing(concreteEvent, force);

        updateEvent(id, noEmails, concreteEvent, foundEvent);

        return crossingData;
    }

    @RequestMapping(value = "/reconcile/event/{eventId}", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK, reason = "Updated")
    @Secured("ROLE_RECONCILE")
    public void editEventFormReconcile(@PathVariable("eventId") Long id,
                                       @RequestParam(name = "force", required = false, defaultValue = "false") Boolean force,
                                       @RequestParam(name = "noEmails", required = false, defaultValue = "false") Boolean noEmails,
                                       @RequestBody ConcreteEvent concreteEvent) {

        ConcreteEvent foundEvent = concreteEventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No such ConcreteEvent"));

        eventValidation.validatePast(foundEvent.getDate(), "You can edit only those events that have already passed in RECONCILE module");
        eventValidation.validatePast(concreteEvent.getDate(), "You can not move an event to the future in RECONCILE module");

//        CrossingData crossingData = eventValidation.validateCrossing(concreteEvent, force);

        updateEvent(id, noEmails, concreteEvent, foundEvent);

//        return crossingData;
    }

    private void updateEvent(Long id, Boolean noEmails, ConcreteEvent newEvent, ConcreteEvent previousEvent) {

        ConcreteEventChange concreteEventChange = createConcreteEventChangeCode(newEvent, previousEvent);

        Event event = newEvent.getEvent();
        if (event != null)
            previousEvent.setEvent(event);

        Service service = newEvent.getService();

        if (service != null)
            previousEvent.setService(service);

        Room room = newEvent.getRoom();
        if (room != null)
            previousEvent.setRoom(room);

        Therapist therapist = newEvent.getTherapist();
        if (therapist != null)
            previousEvent.setTherapist(therapist);

        Client client = newEvent.getClient();
        if (client != null)
            previousEvent.setClient(client);

        Session session = newEvent.getSession();
        if (session != null)
            previousEvent.setSession(session);

        String notes = newEvent.getNote();
        if (!(notes == null || notes.isEmpty()))
            previousEvent.setNote(notes);

        Time time = newEvent.getTime();
        if (time != null)
            previousEvent.setTime(time);

        Date date = newEvent.getDate();
        if (date != null)
            previousEvent.setDate(date);

        String subStatus = newEvent.getSubStatus();
        if (!(subStatus == null || subStatus.isEmpty()))
            previousEvent.setSubStatus(subStatus);

        Duration eventDuration = newEvent.getDuration();
        if (eventDuration != null) {
            Duration foundEventDuration = previousEvent.getDuration();
            if (foundEventDuration == null) {
                previousEvent.setDuration(eventDuration);
            } else {
                Integer prep = eventDuration.getPrep();
                if (prep != null)
                    foundEventDuration.setPrep(prep);
                Integer processing = eventDuration.getProcessing();
                if (processing != null)
                    foundEventDuration.setProcessing(processing);
                Integer clean = eventDuration.getClean();
                if (clean != null)
                    foundEventDuration.setClean(clean);
            }
        }

        ConcreteEventState state = newEvent.getState();
        if (state == ConcreteEventState.confirmed) {
            previousEvent.setState(ConcreteEventState.tentative);
        } else {
            previousEvent.setState(state);
        }

        concreteEventRepository.save(previousEvent);

        wsService.notifyBookingEvent(new BookingEventNotification(previousEvent, CrudAction.UPDATE));

        //FIXME: removed while emails are switched off - in future should be uncommented
        /*
        if (noEmails == null || !noEmails) {
            // remove previous
            concreteEventChangeCodeRepository.deleteByConcreteEventId(id);
            // save new
            concreteEventChangeCodeRepository.save(concreteEventChange);
            mailService.sendEventChange(concreteEventChange, newEvent, getEmailToByConcreteEvent(newEvent.getTherapist()));

        }*/

    }

    @RequestMapping(value = "/booking/concreteCalendarEvent/{eventId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK, reason = "Deleted")
    @Secured("ROLE_BOOKING")
    public void deleteConcreteCalendarEvent(@PathVariable("eventId") Long id) {
        ConcreteCalendarEvent event = concreteCalendarEventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No such ConcreteCalendarEvent"));
        event.setArchived(true);
        concreteCalendarEventRepository.save(event);
    }

    /**
     * update event. only non null fileds will be changed in event. null fileds will preserv value
     *
     * @param id
     * @param noEmails - if true no emails will be send to therapist
     */
    @RequestMapping(value = "/booking/event/{eventId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK, reason = "Deleted")
    @Secured("ROLE_BOOKING")
    public void deleteEvent(@PathVariable("eventId") Long id,
                            @RequestParam(name = "noEmails", required = false, defaultValue = "false") Boolean noEmails) {
        ConcreteEvent event = concreteEventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No such ConcreteEvent"));

        concreteEventReconcileRepository.delete(event.getId());
        concreteEventRepository.delete(event);

        wsService.notifyBookingEvent(new BookingEventNotification(event, CrudAction.DELETE));

        //FIXME: removed while emails are switched off - in future should be uncommented
        /*
        if (noEmails == null || !noEmails) {
            ConcreteEventChange concreteEventChange = createConcreteEventChangeCode(null, event);
            // remove previous
            concreteEventChangeCodeRepository.deleteByConcreteEventId(event.getId());
            // save new
            concreteEventChangeCodeRepository.save(concreteEventChange);

            mailService.sendEventRemoved(event, getEmailToByConcreteEvent(event.getTherapist()));
        }*/
    }

    /**
     * check for intersections without update
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/booking/event/{eventId}/intersections", method = RequestMethod.GET)
    @Secured("ROLE_BOOKING")
    public Collection<CrossingItem> getIntersections(@PathVariable("eventId") Long id) {
        // todo calculate crossings
        return new ArrayList<CrossingItem>();// todo
    }

    /**
     * Create new event
     *
     * @param event
     * @param force    - update time/date event if there are intersections
     * @param noEmails - if true no emails will be send to therapist
     */
    @RequestMapping(value = "/booking/event", method = RequestMethod.POST)
    @Secured("ROLE_BOOKING")
    public ConcreteEvent createEventFromBooking(@RequestBody ConcreteEvent event,
                                                @RequestParam(name = "force", required = false, defaultValue = "false") Boolean force,
                                                @RequestParam(name = "noEmails", required = false, defaultValue = "false") Boolean noEmails) {

        eventValidation.validateCrossing(event, force);
        return createEvent(event, force, noEmails);
    }

    @RequestMapping(value = "/reconcile/event", method = RequestMethod.POST)
    @Secured("ROLE_RECONCILE")
    public ConcreteEvent createEventFromReconcile(@RequestBody ConcreteEvent event,
                                                  @RequestParam(name = "force", required = false, defaultValue = "false") Boolean force,
                                                  @RequestParam(name = "noEmails", required = false, defaultValue = "false") Boolean noEmails) {

        eventValidation.validatePast(event.getDate(), "You can not create an event in the future in RECONCILE module");
        return createEvent(event, force, noEmails);
    }

    public ConcreteEvent createEvent(ConcreteEvent event,
                                     Boolean force,
                                     Boolean noEmails) {

        event.setState(ConcreteEventState.tentative);
        event = concreteEventRepository.save(event);

        createConcreteEventReconcile(event);

        wsService.notifyBookingEvent(new BookingEventNotification(event, CrudAction.CREATE));

        //FIXME: removed while emails are switched off - in future should be uncommented
        /*
        if (noEmails == null || !noEmails) {
            ConcreteEventChange concreteEventChange = createConcreteEventChangeCode(event, null);
            // remove previous
            concreteEventChangeCodeRepository.deleteByConcreteEventId(event.getId());
            // save new
            concreteEventChangeCodeRepository.save(concreteEventChange);

            mailService.sendEventCreated(concreteEventChange, event, getEmailToByConcreteEvent(event.getTherapist()));

        }*/

        return event;
    }

    private void createConcreteEventReconcile(ConcreteEvent event) {
        Service service = event.getService();
        Float cost = (service != null) ? service.getPrice() : event.getEvent().getPrice();

        concreteEventReconcileRepository.save(new ConcreteEventReconcile(event, cost));
    }

    @RequestMapping(value = "/booking/events/subStatus", method = RequestMethod.GET)
    @Secured("ROLE_BOOKING")
    public Collection<ConcreteEventSubStatus> getSubStatuses() {
        return concreteEventSubStatusRepository.findAll();
    }

    @RequestMapping(value = "/booking/event/{eventCode}/lastChange", method = RequestMethod.GET)
    public ConcreteEventChange getEventCode(@PathVariable("eventCode") String eventCode) {
        return concreteEventChangeCodeRepository.findByEventCode(eventCode)
                .orElseThrow(() -> new NotFoundException("No such ConcreteEventChange"));
    }

    @RequestMapping(value = "/booking/event/{eventCode}/confirm", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK, reason = "Confirmed")
    public void confirm(@PathVariable("eventCode") String eventCode) {
        setConcreteEventStateByEventCode(eventCode, ConcreteEventState.confirmed);
    }

    @RequestMapping(value = "/booking/event/{eventCode}/decline", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK, reason = "Declined")
    public void decline(@PathVariable("eventCode") String eventCode) {
        setConcreteEventStateByEventCode(eventCode, ConcreteEventState.cancelled);
    }

    private void setConcreteEventStateByEventCode(String eventCode, ConcreteEventState state) {
        ConcreteEventChange code = concreteEventChangeCodeRepository.findByEventCode(eventCode)
                .orElseThrow(() -> new NotFoundException("No such ConcreteEventChange"));

        Long concreteEventId = code.getConcreteEventId();
        if (concreteEventId != null) {
            concreteEventRepository.findById(concreteEventId).ifPresent(concreteEvent -> {// for add or edit operation (not for delete)
                concreteEvent.setState(state);
                concreteEventRepository.save(concreteEvent);
                wsService.notifyBookingEvent(new BookingEventNotification(concreteEvent, CrudAction.UPDATE));
            });
        }

        concreteEventChangeCodeRepository.delete(code);
    }

    private ConcreteEventChange createConcreteEventChangeCode(ConcreteEvent newEvent, ConcreteEvent previousEvent) {
        ConcreteEventChange eventChangeCode = new ConcreteEventChange();

        eventChangeCode.setEventCode(UUID.randomUUID().toString());

        if (newEvent != null)
            eventChangeCode.setNewValue(generateConcreteEventChangeValue(newEvent));

        if (previousEvent != null)
            eventChangeCode.setPreValue(generateConcreteEventChangeValue(previousEvent));

        if (newEvent != null)
            eventChangeCode.setConcreteEventId(newEvent.getId());
        else if (previousEvent != null)
            eventChangeCode.setConcreteEventId(previousEvent.getId());

        return eventChangeCode;
    }

    private ConcreteEventChangeValue generateConcreteEventChangeValue(ConcreteEvent event) {

        ConcreteEventChangeValue value = new ConcreteEventChangeValue();

        if (event.getService() != null) {
            value.setService(event.getService().getName());
        }

        if (event.getEvent() != null) {
            value.setEvent(event.getEvent().getName());
        }

        if (event.getRoom() != null) {
            value.setRoom(event.getRoom().getName());
        }

        if (event.getTherapist() != null) {
            value.setTherapist(event.getTherapist().getName());
        }

        if (event.getClient() != null) {
            value.setClient(event.getClient().getName());
        }

        if (event.getSession() != null) {
            value.setSession(event.getSession().getName());
        }

        value.setNote(event.getNote());

        value.setTime(event.getTime());

        value.setDate(DateUtils.toDateFormat(event.getDate()));

        value.setSubStatus(event.getSubStatus());

        value.setDurationPrep(event.getDuration().getPrep().toString());

        value.setDurationProcessing(event.getDuration().getProcessing().toString());

        value.setDurationClean(event.getDuration().getClean().toString());

        return value;
    }

    private String getEmailToByConcreteEvent(Therapist therapist) {
        if (therapist != null) {
            return therapist.getEmail();
        }
        return null;
    }
}
