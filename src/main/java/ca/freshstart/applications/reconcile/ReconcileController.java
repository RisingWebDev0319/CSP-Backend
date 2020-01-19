package ca.freshstart.applications.reconcile;


import ca.freshstart.data.concreteEvent.types.ConcreteEventState;
import ca.freshstart.data.reconcile.entity.Audit;
import ca.freshstart.data.reconcile.entity.ConcreteEventReconcile;
import ca.freshstart.data.reconcile.entity.Reconcile;
import ca.freshstart.data.reconcile.repository.AuditRepository;
import ca.freshstart.data.reconcile.repository.ConcreteEventReconcileRepository;
import ca.freshstart.data.reconcile.repository.ReconcileRepository;
import ca.freshstart.data.reconcile.types.ConcreteEventReconcileState;
import ca.freshstart.data.reconcile.types.SignatureInfo;
import ca.freshstart.exceptions.BadRequestException;
import ca.freshstart.types.AbstractController;
import ca.freshstart.types.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class ReconcileController extends AbstractController {

    private final ConcreteEventReconcileRepository concreteEventReconcileRepository;
    private final AuditRepository auditRepository;
    private final ReconcileRepository reconcileRepository;

    /**
     * return all event for date
     *
     * @param date
     * @param dateStart
     * @param dateEnd
     * @param clientId
     * @return
     */
    @RequestMapping(value = "/reconcile/events", method = RequestMethod.GET)
    @Secured("ROLE_RECONCILE")
    public Collection<ConcreteEventReconcile> getEvents(@RequestParam(value = "date", required = false) @DateTimeFormat(pattern = Constants.DATE_FORMAT) Date date,
                                                        @RequestParam(value = "dateStart", required = false) @DateTimeFormat(pattern = Constants.DATE_FORMAT) Date dateStart,
                                                        @RequestParam(value = "dateEnd", required = false) @DateTimeFormat(pattern = Constants.DATE_FORMAT) Date dateEnd,
                                                        @RequestParam(value = "clientId", required = false) Long clientId) {
        if (date != null) {
            if (clientId != null) {
                return concreteEventReconcileRepository.findByDateAndClientId(date, clientId);
            } else {
                return concreteEventReconcileRepository.findByDate(date);
            }
        } else if (dateStart != null && dateEnd != null) {
            if (clientId != null) {
                return concreteEventReconcileRepository.findByDateInRangeAndClientId(dateStart, dateEnd, clientId);
            } else {
                return concreteEventReconcileRepository.findByDateInRange(dateStart, dateEnd);
            }
        } else {
            throw new BadRequestException("Request should have date parameter or pair dateStart & dateEnd");
        }
    }

    /**
     */
    @RequestMapping(value = "/reconcile/audit", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK, reason = "Created")
    @Secured("ROLE_RECONCILE")
    @Transactional
    public void auditEvents(@RequestBody SignatureInfo signatureInfo) {

        Long[] eventsIds = signatureInfo.getEvents();
        if (eventsIds == null || eventsIds.length < 1) {
            throw new BadRequestException("No one event id indicated");
        }

        Set<ConcreteEventReconcile> events = concreteEventReconcileRepository.findInIds(Arrays.asList(eventsIds));

        validateForAudit(events);

        Audit audit = new Audit();
        audit.setUser(getCurrentUser());
        audit.setUserSignature(signatureInfo.getName());
        auditRepository.save(audit);

        events.forEach(concreteEventReconcile -> {
            concreteEventReconcile.setAudit(audit);
            concreteEventReconcile.setReconcileState(ConcreteEventReconcileState.audited);
        });
        concreteEventReconcileRepository.save(events);
    }

    private void validateForAudit(Set<ConcreteEventReconcile> events) {
        if (events.isEmpty()) {
            throw new BadRequestException("A list of ConcreteEventReconcile for audit is empty");
        }

        boolean allMatch = events.stream().allMatch(event -> event.getConcreteEvent().getState() == ConcreteEventState.completed);
        if (!allMatch) {
            throw new BadRequestException("Not all concrete events have 'completed' state.");
        }
    }

    /**
     */
    @RequestMapping(value = "/reconcile/reconcile", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK, reason = "Created")
    @Secured("ROLE_RECONCILE")
    @Transactional
    public void reconcileEvents(@RequestBody SignatureInfo signatureInfo) {

        Long[] eventsIds = signatureInfo.getEvents();
        if (eventsIds == null || eventsIds.length < 1) {
            throw new BadRequestException("No one event id indicated");
        }

        Set<ConcreteEventReconcile> events = concreteEventReconcileRepository.findInIds(Arrays.asList(eventsIds));

        validateForReconcile(events);

        Reconcile reconcile = new Reconcile();
        reconcile.setUser(getCurrentUser());
        reconcile.setUserSignature(signatureInfo.getName());
        reconcileRepository.save(reconcile);

        events.forEach(concreteEventReconcile -> {
            concreteEventReconcile.setReconcile(reconcile);
            concreteEventReconcile.setReconcileState(ConcreteEventReconcileState.reconciled);
        });
        concreteEventReconcileRepository.save(events);
    }

    private void validateForReconcile(Set<ConcreteEventReconcile> events) {
        if (events.isEmpty()) {
            throw new BadRequestException("A list of ConcreteEventReconcile for reconcile is empty");
        }

        boolean allMatch = events.stream().allMatch(event -> event.getReconcileState() == ConcreteEventReconcileState.audited);
        if (!allMatch) {
            throw new BadRequestException("Not all ConcreteEventReconcile audited");
        }
    }

}
