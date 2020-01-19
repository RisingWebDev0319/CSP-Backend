package ca.freshstart.applications.event;

import ca.freshstart.data.event.entity.Event;
import ca.freshstart.data.eventTypes.entity.EventTypes;
import ca.freshstart.data.eventTypes.repository.EventTypesRepository;
import ca.freshstart.data.taxes.entity.Taxes;
import ca.freshstart.data.taxes.repository.TaxesRepository;
import ca.freshstart.exceptions.NotFoundException;
import ca.freshstart.types.Count;
import ca.freshstart.data.event.repository.EventRepository;
import ca.freshstart.helpers.CspUtils;
import ca.freshstart.types.IdResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import ca.freshstart.types.AbstractController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class EventController extends AbstractController {

    private final EventRepository eventRepository;
    private final TaxesRepository taxesRepository;
    private final EventTypesRepository eventTypesRepository;

    /**
     * Return list of events
     *
     * @param pageId   page to show
     * @param pageSize Item on page to show (10 by default)
     * @param sort     Field to sort by, +/- prefix defines order (ACS/DESC)
     * @return Requested events
     */
    @RequestMapping(value = "/events", method = RequestMethod.GET)
    @Secured("ROLE_SETTINGS")
    public List<Event> getList(@RequestParam(value = "pageId", required = false, defaultValue = "1") int pageId,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                               @RequestParam(value = "sort", required = false) String sort) {
        return eventRepository.findAllAsList(new PageRequest(pageId - 1, pageSize, CspUtils.createSort(sort)));
    }

    /**
     * Return count of events
     *
     * @return Count of events
     */
    @RequestMapping(value = "/events/count", method = RequestMethod.GET)
    @Secured("ROLE_SETTINGS")
    public Count eventsCount() {
        return new Count(eventRepository.count());
    }

    @RequestMapping(value = "/events/{eventId}", method = RequestMethod.GET)
    @Secured({"ROLE_SETTINGS"})
    public Event getEventById(@PathVariable("eventId") Long eventId) {

        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("No such Event"));
    }

    @RequestMapping(value = "/events", method = RequestMethod.POST)
    @Secured("ROLE_SETTINGS")
    public IdResponse createEvent(@RequestBody Event event) {
        if(event.getTax() != null){
            Taxes existTax = taxesRepository.findOne(event.getTax().getId());
            event.setTax(existTax);
        }
        if(event.getEventType() != null){
            EventTypes existType = eventTypesRepository.findOne(event.getEventType().getId());
            event.setEventType(existType);
        }

//        event.setTax(null);
//        event.setEventType(null);
//        event = eventRepository.save(event);

        eventRepository.save(event);
        return new IdResponse(event.getId());
    }

    @RequestMapping(value = "/events/{eventId}", method = RequestMethod.PUT)
    @Secured("ROLE_SETTINGS")
    public void updateEvent(@PathVariable("eventId") Long eventId,
                            @RequestBody Event event) {
        eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("No such event"));
        eventRepository.save(event);
    }

    @RequestMapping(value = "/events/{eventId}", method = RequestMethod.DELETE)
    @Secured("ROLE_SETTINGS")
    public void deleteEvent(@PathVariable("eventId") Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("No such eventType"));
        event.setArchived(true);
        eventRepository.save(event);
    }
}
