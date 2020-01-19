package ca.freshstart.applications.estimate;

import ca.freshstart.data.reconcile.entity.ConcreteEventReconcile;
import ca.freshstart.data.reconcile.entity.Estimate;
import ca.freshstart.data.reconcile.repository.ConcreteEventReconcileRepository;
import ca.freshstart.data.reconcile.repository.EstimateRepository;
import ca.freshstart.data.reconcile.types.ConcreteEventEstimateState;
import ca.freshstart.data.reconcile.types.ConcreteEventReconcileState;
import ca.freshstart.exceptions.BadRequestException;
import ca.freshstart.exceptions.NotFoundException;
import ca.freshstart.helpers.CspUtils;
import ca.freshstart.helpers.DateUtils;
import ca.freshstart.helpers.remote.RemoteSettingsManager;
import ca.freshstart.types.Count;
import ca.freshstart.types.EstimateIdResponse;
import ca.freshstart.types.IdResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;

@RestController
@RequiredArgsConstructor
public class EstimateController {
    private final EstimateRepository estimateRepository;
    private final ConcreteEventReconcileRepository concreteEventReconcileRepository;
    private final RemoteSettingsManager remoteManager;


    @RequestMapping(value = "/estimates", method = RequestMethod.GET)
    @Secured("ROLE_ESTIMATE")
    public Collection<Estimate> getLastEstimates(@RequestParam(value = "pageId", required = false, defaultValue = "1") int pageId,
                                                 @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                                                 @RequestParam(value = "sort", required = false) String sort) {
        List<Estimate> list = new ArrayList<>();

        PageRequest pageRequest = new PageRequest(pageId - 1, pageSize, CspUtils.createSort(sort));

        estimateRepository.findAll(pageRequest).forEach(list::add);

        return list;
    }


    @RequestMapping(value = "/estimates/count", method = RequestMethod.GET)
    @Secured("ROLE_ESTIMATE")
    public Count getLastEstimatesCount() {
        return new Count(estimateRepository.countAll());
    }


    @RequestMapping(value = "/estimates/client/{clientId}", method = RequestMethod.GET)
    @Secured("ROLE_ESTIMATE")
    public Estimate getClientLastEstimates(@PathVariable("clientId") Long clientId) {

        Map<Date, Estimate> date2Estimate = estimateRepository.findUnsentByClientId(clientId)
                .stream()
                .collect(Collectors.toMap((Estimate e) -> {
                    List<Date> eventsDates = e.getEvents().stream()
                            .map(concreteEventReconcile -> concreteEventReconcile.getConcreteEvent().getDate())
                            .collect(Collectors.toList());
                    return getLast(eventsDates);
                }, e -> e));

        Date lastDate = getLast(new ArrayList<>(date2Estimate.keySet()));
        return lastDate == null
                ? null
                : date2Estimate.get(lastDate);
    }

    private Date getLast(List<Date> dates) {
        if (dates == null || dates.isEmpty()) {
            return null;
        }
        Date lastDate = dates.get(0);
        for (Date eventsDate : dates) {
            if (eventsDate.getTime() > lastDate.getTime()) {
                lastDate = eventsDate;
            }
        }
        return lastDate;

    }


    @RequestMapping(value = "/estimates/client/{clientId}", method = RequestMethod.POST)
    @Secured("ROLE_ESTIMATE")
    public IdResponse createEstimate(@PathVariable("clientId") Long clientId,
                                     @RequestBody Long[] eventsIds) {
        ArrayList<Long> ids = new ArrayList<>(Arrays.asList(eventsIds));
        Set<ConcreteEventReconcile> events = concreteEventReconcileRepository.findInIds(ids);

        validateEvents(events, clientId);

        Estimate estimate = new Estimate();
        estimate.setClientId(clientId);
        estimate.setSent(false);
        estimateRepository.save(estimate);

        events.forEach(event -> {
            event.setEstimate(estimate);
            setEstimationStatus(event);
        });
        concreteEventReconcileRepository.save(events);

        return new IdResponse(estimate.getId());
    }


    @RequestMapping(value = "/estimates/{estimateId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK, reason = "Deleted")
    @Secured("ROLE_ESTIMATE")
    public void deleteEstimate(@PathVariable("estimateId") Long estimateId) {
        Estimate estimate = estimateRepository.findUnsentById(estimateId)
                .orElseThrow(() -> new NotFoundException("No such unsent Estimate"));

        Set<ConcreteEventReconcile> events = estimate.getEvents();
        events.forEach(event ->{
            event.setEstimate(null);
            unsetEstimationStatus(event);
        });
        concreteEventReconcileRepository.save(events);
        estimateRepository.delete(estimateId);
    }


    @RequestMapping(value = "/estimates/{estimateId}", method = RequestMethod.GET)
    @Secured("ROLE_ESTIMATE")
    public Estimate getEstimate(@PathVariable("estimateId") Long estimateId) {
        return estimateRepository.findById(estimateId)
                .orElseThrow(() -> new NotFoundException("No such unsent Estimate"));
    }

    @RequestMapping(value = "/estimates/{estimateId}/send", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK, reason = "Sent")
    @Secured("ROLE_ESTIMATE")
    public void sendEstimate(@PathVariable("estimateId") Long estimateId) {
        Estimate estimate = estimateRepository.findUnsentById(estimateId)
                .orElseThrow(() -> new NotFoundException("No such unsent Estimate"));

        if (estimate.isSent()) {
            throw new BadRequestException("The Estimate have already sent");
        }

        EstimateIdResponse estimateIdResponse = remoteManager.saveEstimateToRemote(estimate);

        if (estimateIdResponse != null) {
            estimate.setExternalId(estimateIdResponse.getEstimateId());
            estimate.setSent(true);
            estimateRepository.save(estimate);

            Set<ConcreteEventReconcile> events = estimate.getEvents();
            events.forEach(event -> event.setEstimateState(ConcreteEventEstimateState.external));
            concreteEventReconcileRepository.save(events);
        }
    }

    @RequestMapping(value = "/estimates/{estimateId}/events", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK, reason = "Updated")
    @Secured("ROLE_ESTIMATE")
    public void appendToEstimate(@PathVariable("estimateId") Long estimateId,
                                 @RequestBody Long[] eventsIds) {
        ArrayList<Long> ids = new ArrayList<>(Arrays.asList(eventsIds));
        Set<ConcreteEventReconcile> events = concreteEventReconcileRepository.findInIds(ids);

        Estimate estimate = estimateRepository.findUnsentById(estimateId)
                .orElseThrow(() -> new NotFoundException("No such unsent Estimate"));

        Long clientId = estimate.getClientId();

        validateEvents(events, clientId);

        Map<Long, ConcreteEventReconcile> id2Event = estimate.getEvents().stream()
                .collect(Collectors.toMap(ConcreteEventReconcile::getId, o -> o));

        Set<ConcreteEventReconcile> eventsToAppend = events.stream()
                .filter(event -> id2Event.get(event.getId()) == null)
                .collect(Collectors.toSet());

        eventsToAppend.forEach(event -> {
            event.setEstimate(estimate);
            setEstimationStatus(event);
        });
        concreteEventReconcileRepository.save(eventsToAppend);
    }


    @RequestMapping(value = "/estimates/{estimateId}/events/{eventId}", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK, reason = "Updated")
    @Secured("ROLE_ESTIMATE")
    public void appendOneToEstimate(@PathVariable("estimateId") Long estimateId,
                                    @PathVariable("eventId") Long eventId) {
        Estimate estimate = estimateRepository.findUnsentById(estimateId)
                .orElseThrow(() -> new NotFoundException("No such unsent Estimate"));

        ConcreteEventReconcile event = concreteEventReconcileRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("No such ConcreteEventReconcile"));

        Long clientId = estimate.getClientId();

        Set<ConcreteEventReconcile> events = new HashSet<>();
        events.add(event);
        validateEvents(events, clientId);

        Optional<ConcreteEventReconcile> found = estimate.getEvents().stream()
                .filter(concreteEventReconcile -> concreteEventReconcile.getId().equals(eventId))
                .findFirst();
        if (found.isPresent()) {
            throw new BadRequestException(format("The event (%d) already in the estimation", eventId));
        }

        event.setEstimate(estimate);
        setEstimationStatus(event);
        concreteEventReconcileRepository.save(event);
    }

    @RequestMapping(value = "/estimates/{estimateId}/events/{eventId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK, reason = "Deleted")
    @Secured("ROLE_ESTIMATE")
    public void deleteFromEstimate(@PathVariable("estimateId") Long estimateId,
                                   @PathVariable("eventId") Long eventId) {
        Estimate estimate = estimateRepository.findUnsentById(estimateId)
                .orElseThrow(() -> new NotFoundException("No such unsent Estimate"));

        ConcreteEventReconcile event = concreteEventReconcileRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("No such ConcreteEventReconcile"));


        Optional<ConcreteEventReconcile> found = estimate.getEvents().stream()
                .filter(concreteEventReconcile -> concreteEventReconcile.getId().equals(eventId))
                .findFirst();
        if (!found.isPresent()) {
            throw new BadRequestException(format("The event (%d) not found in the estimation (%d)", eventId, estimateId));
        }

        event.setEstimate(null);
        unsetEstimationStatus(event);
        concreteEventReconcileRepository.save(event);
    }

    private void validateEvents(Set<ConcreteEventReconcile> events, Long clientId) {
        if (events.isEmpty()) {
            throw new BadRequestException("A list of ConcreteEventReconcile for estimation is empty");
        }

        boolean allTheSameClient = events.stream().allMatch(event -> event.getConcreteEvent().getClient().getId().equals(clientId));
        if (!allTheSameClient) {
            throw new BadRequestException("Not all ConcreteEventReconcile have the same client");
        }

        boolean allReconciledAndNotEstimated = events.stream().allMatch(event ->
                event.getReconcileState() == ConcreteEventReconcileState.reconciled
                        && event.getEstimateState() == ConcreteEventEstimateState.none);
        if (!allReconciledAndNotEstimated) {
            throw new BadRequestException("Not all ConcreteEventReconcile reconciled or some ConcreteEventReconcile not estimated");
        }

        Date today = DateUtils.getDayStart(new Date());

        boolean allInThePast = events.stream().allMatch(concreteEventReconcile -> {
            Date date = concreteEventReconcile.getConcreteEvent().getDate();
            Date startOfDate = DateUtils.getDayStart(date);
            return startOfDate.getTime() < today.getTime();
        });
        if (!allInThePast) {
            throw new BadRequestException("Not all ConcreteEventReconcile in the past");
        }

    }

    private ConcreteEventReconcile setEstimationStatus(ConcreteEventReconcile event) {
        event.setReconcileState(ConcreteEventReconcileState.estimate);
        event.setEstimateState(ConcreteEventEstimateState.inner);
        return event;
    }

    private ConcreteEventReconcile unsetEstimationStatus(ConcreteEventReconcile event) {
        event.setReconcileState(ConcreteEventReconcileState.reconciled);
        event.setEstimateState(ConcreteEventEstimateState.none);
        return event;
    }

}
