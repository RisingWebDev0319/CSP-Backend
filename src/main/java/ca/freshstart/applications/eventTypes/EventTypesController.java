package ca.freshstart.applications.eventTypes;

import ca.freshstart.data.event.entity.Event;
import ca.freshstart.data.eventTypes.repository.EventTypesRepository;
import ca.freshstart.data.eventTypes.entity.EventTypes;
import ca.freshstart.data.event.repository.EventRepository;
import ca.freshstart.exceptions.NotFoundException;
import ca.freshstart.helpers.CspUtils;
import ca.freshstart.types.Count;
import ca.freshstart.types.IdResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class EventTypesController {

    @Autowired
    private EventTypesRepository eventTypesRepository;

    @Autowired
    private EventRepository eventRepository;

    @RequestMapping(value = "/eventTypes", method = RequestMethod.GET)
    @Secured({"ROLE_SETTINGS"})
    public List<EventTypes> eventTypes(
            @RequestParam(value = "pageId", required = false, defaultValue = "1") int pageId,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
            @RequestParam(value = "sort", required = false) String sort) {

        return eventTypesRepository.findAllAsList(new PageRequest(pageId - 1, pageSize, CspUtils.createSort(sort)));
    }

    @RequestMapping(value = "/eventTypes/count", method = RequestMethod.GET)
    @Secured("ROLE_SETTINGS")
    public Count eventsCount() {
        return new Count(eventTypesRepository.count());
    }

    @RequestMapping(value = "/eventTypes/{eventTypesId}", method = RequestMethod.GET)
    @Secured({"ROLE_SETTINGS"})
    public EventTypes getEventTypeById(@PathVariable("eventTypesId") Long eventTypesId) {

        return eventTypesRepository.findById(eventTypesId)
                .orElseThrow(() -> new NotFoundException("No such EventType"));
    }

    @RequestMapping(value = "/eventTypes", method = RequestMethod.POST)
    @Secured("ROLE_SETTINGS")
    public IdResponse createEventTypes(@RequestBody EventTypes eventTypes) {

        eventTypes = eventTypesRepository.save(eventTypes);
        return new IdResponse(eventTypes.getId());
    }

    @RequestMapping(value = "/eventTypes/{eventTypeId}", method = RequestMethod.PUT)
    @Secured("ROLE_SETTINGS")
    public void updateEventTypes(@PathVariable("eventTypeId") Long eventTypeId,
                                 @RequestBody EventTypes eventTypes) {
        eventTypesRepository.findById(eventTypeId)
                .orElseThrow(() -> new NotFoundException("No such eventType"));
        eventTypesRepository.save(eventTypes);
    }

    @RequestMapping(value = "/eventTypes/{eventTypeId}", method = RequestMethod.DELETE)
    @Secured("ROLE_SETTINGS")
    public void deleteEventType(@PathVariable("eventTypeId") Long eventTypeId) {
        EventTypes eventType = eventTypesRepository.findById(eventTypeId)
                .orElseThrow(() -> new NotFoundException("No such eventType"));
        eventType.setArchived(true);

        List<Event> eventsList = eventRepository.findByTypeId(eventTypeId);
        if (eventsList.size() > 0) {
            eventsList.stream().map(event -> {
                        event.setEventType(null);
                        return event;
                    }
            )
                    .collect(Collectors.toList());
            eventRepository.save(eventsList);
        }
        eventTypesRepository.save(eventType);
    }

}
