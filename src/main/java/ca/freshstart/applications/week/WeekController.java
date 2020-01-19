package ca.freshstart.applications.week;


import ca.freshstart.data.week.entity.Week;
import ca.freshstart.data.week.repository.WeekRepository;
import ca.freshstart.helpers.CspUtils;
import ca.freshstart.types.AbstractController;
import ca.freshstart.types.Count;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class WeekController extends AbstractController {
    private final WeekRepository WeekRepository;

    /**
     * Return list of events
     *
     * @param pageId   page to show
     * @param pageSize Item on page to show (10 by default)
     * @param sort     Field to sort by, +/- prefix defines order (ACS/DESC)
     * @return Requested events
     */
    @RequestMapping(value = "/week", method = RequestMethod.GET)
    @Secured("ROLE_BOOKING")
    public List<Week> getList(@RequestParam(value = "pageId", required = false, defaultValue = "1") int pageId,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                               @RequestParam(value = "sort", required = false) String sort) {
        return WeekRepository.findAllAsList(new PageRequest(pageId - 1, pageSize, CspUtils.createSort(sort)));
    }

    /**
     * Return count of events
     *
     * @return Count of events
     */
    @RequestMapping(value = "/week/count", method = RequestMethod.GET)
    @Secured("ROLE_BOOKING")
    public Count eventsCount() {
        return new Count(WeekRepository.count());
    }

//    @RequestMapping(value = "/week/{weekId}", method = RequestMethod.GET)
//    @Secured({"ROLE_SETTINGS"})
//    public therapistWeek getEventById(@PathVariable("eventId") Long eventId) {
//
//        return therapistWeekRepository.findById(eventId)
//                .orElseThrow(() -> new NotFoundException("No such Event"));
//    }
//
//    @RequestMapping(value = "/events", method = RequestMethod.POST)
//    @Secured("ROLE_SETTINGS")
//    public IdResponse createEvent(@RequestBody Event event) {
//        if(event.getTax() != null){
//            Taxes existTax = taxesRepository.findOne(event.getTax().getId());
//            event.setTax(existTax);
//        }
//        if(event.getEventType() != null){
//            EventTypes existType = eventTypesRepository.findOne(event.getEventType().getId());
//            event.setEventType(existType);
//        }
//
////        event.setTax(null);
////        event.setEventType(null);
////        event = eventRepository.save(event);
//
//        eventRepository.save(event);
//        return new IdResponse(event.getId());
//    }
//
//    @RequestMapping(value = "/events/{eventId}", method = RequestMethod.PUT)
//    @Secured("ROLE_SETTINGS")
//    public void updateEvent(@PathVariable("eventId") Long eventId,
//                            @RequestBody Event event) {
//        eventRepository.findById(eventId)
//                .orElseThrow(() -> new NotFoundException("No such event"));
//        eventRepository.save(event);
//    }
//
//    @RequestMapping(value = "/events/{eventId}", method = RequestMethod.DELETE)
//    @Secured("ROLE_SETTINGS")
//    public void deleteEvent(@PathVariable("eventId") Long eventId) {
//        Event event = eventRepository.findById(eventId)
//                .orElseThrow(() -> new NotFoundException("No such eventType"));
//        event.setArchived(true);
//        eventRepository.save(event);
//    }
}
