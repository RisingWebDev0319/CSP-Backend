package ca.freshstart.applications.concreteCalendarEvent;

import ca.freshstart.types.AbstractController;
import ca.freshstart.data.appUser.entity.AppUser;
import ca.freshstart.data.therapist.entity.Therapist;
import ca.freshstart.data.calendarEvent.entity.CalendarEvent;
import ca.freshstart.data.concreteCalendarEvent.entity.ConcreteCalendarEvent;
import ca.freshstart.exceptions.BadRequestException;
import ca.freshstart.exceptions.ConflictException;
import ca.freshstart.exceptions.NotFoundException;
import ca.freshstart.data.matching.types.CrossingData;
import ca.freshstart.data.calendarEvent.repository.CalendarEventRepository;
import ca.freshstart.data.concreteCalendarEvent.repository.ConcreteCalendarEventRepository;
import ca.freshstart.data.therapist.repository.TherapistRepository;
import ca.freshstart.data.availability.repository.AvTherapistDayRecordRepository;
import ca.freshstart.data.concreteEvent.repositories.ConcreteEventRepository;
import ca.freshstart.data.suggestions.repository.PreliminaryEventRepository;
import ca.freshstart.services.EventValidationService;
import ca.freshstart.types.Constants;
import ca.freshstart.helpers.ModelValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static ca.freshstart.helpers.DateUtils.isDatesFromToValid;
import static java.util.stream.Collectors.toList;

@RestController
@RequiredArgsConstructor
public class ConcreteCalendarEventController extends AbstractController {

    private final ConcreteCalendarEventRepository concreteCalendarEventRepository;
    private final CalendarEventRepository calendarEventRepository;
    private final TherapistRepository therapistRepository;
    private final ConcreteEventRepository concreteEventRepository;
    private final AvTherapistDayRecordRepository avTherapistDayRecordRepository;
    private final PreliminaryEventRepository preliminaryEventRepository;
    private final EventValidationService EventValidationService;

    /**
     *  Return all concrete event for dates
     * @param dateStart begin date to select concrete calendar events
     * @param dateEnd begin date to select concrete calendar events
     * @return Requested rooms
     */
    @RequestMapping(value = "/calendarEvents/concrete", method = RequestMethod.GET)
    @Secured("ROLE_EVENTS")
    public Collection<ConcreteCalendarEvent> findAll(@RequestParam("dateStart") @DateTimeFormat(pattern = Constants.DATE_FORMAT) Date dateStart,
                                                     @RequestParam("dateEnd") @DateTimeFormat(pattern = Constants.DATE_FORMAT)   Date dateEnd) {

        if(!isDatesFromToValid(dateStart, dateEnd)) {
            throw new BadRequestException("DateStart should be before or equal to DateEnd");
        }

        return concreteCalendarEventRepository.findAllInRange(dateStart, dateEnd);
    }

    @RequestMapping(value = "/calendarEvents/concrete/mine", method = RequestMethod.GET)
    @Secured("ROLE_THERAPIST")
    public Collection<ConcreteCalendarEvent> findMine(@RequestParam("dateStart") @DateTimeFormat(pattern = Constants.DATE_FORMAT) Date dateStart,
                                                      @RequestParam("dateEnd") @DateTimeFormat(pattern = Constants.DATE_FORMAT) Date dateEnd) {
        AppUser user = getCurrentUser();

        Therapist therapist = therapistRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new BadRequestException("User is not a therapist"));

        if(!isDatesFromToValid(dateStart, dateEnd)) {
            throw new BadRequestException("DateStart should be before or equal to DateEnd");
        }

        return concreteCalendarEventRepository.findByTherapistAndDateInRange(therapist.getId(), dateStart, dateEnd);
    }

    /**
     *  Return all concrete event for calendar event
     * @param calendarEventId id of calendar event
     * @param dateStart begin date to select concrete calendar events
     * @param dateEnd begin date to select concrete calendar events
     * @return Requested rooms
     */
    @RequestMapping(value = "/calendarEvents/{calendarEventId}/concrete", method = RequestMethod.GET)
    @Secured("ROLE_EVENTS")
    public Collection<ConcreteCalendarEvent> calendarEventsConcrete(@PathVariable("calendarEventId") Long calendarEventId,
                                                                    @RequestParam("dateStart") @DateTimeFormat(pattern = Constants.DATE_FORMAT) Date dateStart,
                                                                    @RequestParam("dateEnd") @DateTimeFormat(pattern = Constants.DATE_FORMAT) Date dateEnd) {

        if(!isDatesFromToValid(dateStart, dateEnd)) {
            throw new BadRequestException("DateStart should be before or equal to DateEnd");
        }

        calendarEventRepository.findById(calendarEventId)
                               .orElseThrow(() -> new NotFoundException("No such calendar event"));

        return concreteCalendarEventRepository.findInRange(calendarEventId, dateStart, dateEnd);
    }

    /**
     * Update concrete calendar event
     * @param calendarEventId id of calendar event
     * @param concreteCalendarEventId id of concrete calendar event
     * @param calendarEventFrom Updated concrete calendar event
     */
    @RequestMapping(value = "/calendarEvents/{calendarEventId}/concrete/{concreteCalendarEventId}", method = RequestMethod.PUT)
    @ResponseStatus(value= HttpStatus.OK, reason = "Updated")
    @Secured("ROLE_EVENTS")
    public CrossingData update(@PathVariable("calendarEventId")         Long calendarEventId,
                               @PathVariable("concreteCalendarEventId") Long concreteCalendarEventId,
                               @RequestParam(name = "force", required = false, defaultValue = "false") Boolean force,
                               @RequestBody ConcreteCalendarEvent calendarEventFrom) {

        CrossingData crossingData = EventValidationService.validateCrossing(calendarEventFrom, force);

        CalendarEvent calendarEvent = calendarEventRepository.findById(calendarEventId)
                .orElseThrow(() -> new NotFoundException("No such calendar event"));

        ModelValidation.validateConcreteCalendarEvent(calendarEventFrom, calendarEvent);

        ConcreteCalendarEvent concreteCalendarEvent = concreteCalendarEventRepository.findById(concreteCalendarEventId)
                              .orElseThrow(() -> new NotFoundException("No such concrete calendar event"));

        concreteCalendarEvent.setDate(calendarEventFrom.getDate());
        concreteCalendarEvent.setRoom(calendarEventFrom.getRoom());
        concreteCalendarEvent.setTherapist(calendarEventFrom.getTherapist());
        concreteCalendarEvent.setTime(calendarEventFrom.getTime());
        concreteCalendarEvent.setClients(calendarEventFrom.getClients());
        concreteCalendarEvent.setChanged(true);

        try {
            concreteCalendarEventRepository.save(concreteCalendarEvent);
        } catch(DataIntegrityViolationException ex) {
            throw new ConflictException("Name should be unique and not empty");
        }

        return crossingData;
    }


    /**
     * Delete concrete calendar event
     * @param calendarEventId id of calendar event
     * @param concreteCalendarEventId id of concrete calendar event
     */
    @RequestMapping(value = "/calendarEvents/{calendarEventId}/concrete/{concreteCalendarEventId}", method = RequestMethod.DELETE)
    @ResponseStatus(value= HttpStatus.OK, reason = "Deleted")
    @Secured("ROLE_EVENTS")
    public void deleteRoomById(@PathVariable("calendarEventId")         Long calendarEventId,
                               @PathVariable("concreteCalendarEventId") Long concreteCalendarEventId) {

        calendarEventRepository.findById(calendarEventId)
                               .orElseThrow(() -> new NotFoundException("No such calendar event"));

        ConcreteCalendarEvent concreteCalendarEvent = concreteCalendarEventRepository.findByConcreteId(calendarEventId, concreteCalendarEventId)
                              .orElseThrow(() -> new NotFoundException("No such concrete calendar event"));

        concreteCalendarEvent.setArchived(true);

        concreteCalendarEventRepository.save(concreteCalendarEvent);
    }
}
