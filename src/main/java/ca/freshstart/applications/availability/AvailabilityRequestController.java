package ca.freshstart.applications.availability;

import ca.freshstart.types.AbstractController;
import ca.freshstart.data.therapist.entity.Therapist;
import ca.freshstart.data.availability.entity.AvRequest;
import ca.freshstart.data.availability.entity.AvTherapistDayRecord;
import ca.freshstart.data.availability.entity.AvTherapistRequest;
import ca.freshstart.exceptions.BadRequestException;
import ca.freshstart.exceptions.ConflictException;
import ca.freshstart.exceptions.NotFoundException;
import ca.freshstart.types.Count;
import ca.freshstart.types.IdResponse;
import ca.freshstart.applications.availability.types.MessageRequest;
import ca.freshstart.data.availability.types.AvTherapistRequestStatus;
import ca.freshstart.data.therapist.repository.TherapistRepository;
import ca.freshstart.data.availability.repository.AvRequestRepository;
import ca.freshstart.data.availability.repository.AvTherapistDayRecordRepository;
import ca.freshstart.data.availability.repository.AvTherapistRequestRepository;
import ca.freshstart.helpers.CspUtils;
import ca.freshstart.helpers.ModelValidation;
import ca.freshstart.helpers.TaskManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Function;

import static ca.freshstart.helpers.CspUtils.isNullOrEmpty;
import static ca.freshstart.helpers.ModelValidation.validateAvailTherapistDayRecord;

@RestController
@RequiredArgsConstructor
public class AvailabilityRequestController extends AbstractController {

    private final AvRequestRepository availabilityRequestRepository;
    private final AvTherapistRequestRepository availabilityTherapistRequestRepository;
    private final AvTherapistDayRecordRepository availabilityTherapistDayRecordRepository;
    private final TherapistRepository therapistRepository;
    private final TaskManager taskManager;

    @Value("${server.frontend:}")
    protected String frontendPath;
    @Value("${email.template.therapistEmail:}")
    protected String therapistEmail;
    @Value("${email.template.therapistAvailabilityRequests:}")
    protected String therapistAvailabilityRequests;

    /**
     * Return list of all current requests for availability
     *
     * @param pageId   page to show
     * @param pageSize Item on page to show (10 by default)
     * @param sort     Field to sort by, +/- prefix defines order (ACS/DESC)
     * @return Requested availability requests
     */
    @RequestMapping(value = "/availability/requests", method = RequestMethod.GET)
    @Secured("ROLE_AVAILABILITY")
    public Collection<AvRequest> list(@RequestParam(value = "pageId", required = false, defaultValue = "1") int pageId,
                                      @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                                      @RequestParam(value = "sort", required = false) String sort) {

        return availabilityRequestRepository.findAllAsList(new PageRequest(pageId - 1, pageSize, CspUtils.createSort(sort)));
    }

    /**
     * Return count of requests for availability
     *
     * @return Count of requests for availability
     */
    @RequestMapping(value = "/availability/requests/count", method = RequestMethod.GET)
    @Secured("ROLE_AVAILABILITY")
    public Count count() {
        return new Count(availabilityRequestRepository.count());
    }

    /**
     * Add new request
     *
     * @param request request to add
     * @return Id of the request
     */
    @RequestMapping(value = "/availability/request", method = RequestMethod.POST)
    @Secured("ROLE_AVAILABILITY")
    public IdResponse addNew(@RequestBody AvRequest request) {

        ModelValidation.validateAvailabilityRequest(request);

        List<AvTherapistRequest> therapistRequests = request.getTherapistsRequests();

        therapistRequests.forEach(req -> {
            req.setAvRequest(request);
        });

        try {
            availabilityRequestRepository.save(request);
        } catch (DataIntegrityViolationException ex) {
            // todo unique ?
            throw new ConflictException("Name should be unique and not empty");
        }

        sendTherapistAvailabilityRequests(therapistRequests, request.getId());

        return new IdResponse(request.getId());
    }

    /***
     * @param requestId to update
     * @return AvTherapistRequest
     */
    @RequestMapping(value = "/availability/request/{requestId}", method = RequestMethod.PUT)
    @Secured("ROLE_AVAILABILITY")
    public IdResponse updateRequest(@RequestBody AvRequest requestBody, @PathVariable("requestId") Long requestId) {

        //find edit element from BD
        AvRequest request = availabilityRequestRepository.findById(requestId).orElseThrow(() -> new NotFoundException("No such request"));
        //Validate request
        ModelValidation.validateAvailabilityRequest(requestBody);
        List<AvTherapistRequest> therapistRequests = requestBody.getTherapistsRequests();
        therapistRequests.forEach((AvTherapistRequest rapist) -> {
            rapist.setAvRequest(request);
            //Set Id for correct updating
            request.getTherapistsRequests().forEach((x) -> {
                if ((x.getAvRequest().getId() == rapist.getAvRequest().getId()) &&
                        (x.getTherapistId() == rapist.getTherapistId()))
                    rapist.setId(x.getId());
            });
        });
        request.getTherapistsRequests().clear();
        request.getTherapistsRequests().addAll(therapistRequests);
        //request.setTherapistsRequests(therapistRequests);
        try {
            availabilityRequestRepository.save(request);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Name should be unique and not empty");
        }

        sendTherapistAvailabilityRequests(therapistRequests, requestBody.getId());

        return new IdResponse(requestBody.getId());
    }

    /**
     * Sending mails as loop
     * @param therapistRequests
     * @param requestId
     */
    private void sendTherapistAvailabilityRequests(List<AvTherapistRequest> therapistRequests, Long requestId) {
        if (!isNullOrEmpty(therapistRequests)) {

            therapistRequests.forEach(tr -> {
                therapistRepository.findById(tr.getTherapistId()).ifPresent(therapist -> {
                    taskManager.submitTask(() -> {
                        //FIXME: removed while emails are switched off - in future should be uncommented
                        //mailService.sendAvailabilityCreated(therapist.getEmail(),tr, requestId);
                    });
                });
            });
        }
    }

    /**
     * Return information about one request
     *
     * @param requestId id of request
     * @return Request data
     */
    @RequestMapping(value = "/availability/request/{requestId}", method = RequestMethod.GET)
    @Secured("ROLE_AVAILABILITY")
    public AvRequest getById(@PathVariable("requestId") Long requestId) {

        return availabilityRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("No such request"));
    }

    /**
     * Move request to archive or delete (if all items are created or viewed)
     *
     * @param requestId id of request
     */
    @RequestMapping(value = "/availability/request/{requestId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK, reason = "Deleted")
    @Secured("ROLE_AVAILABILITY")
    public void deleteById(@PathVariable("requestId") Long requestId) {

        AvRequest request = availabilityRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("No such request"));

        List<AvTherapistRequest> therapistRequests = request.getTherapistsRequests();

        for (AvTherapistRequest tr: therapistRequests){
            therapistRepository.findById(tr.getTherapistId()).ifPresent(therapist -> {
                taskManager.submitTask(() -> {
                    //FIXME: removed while emails are switched off - in future should be uncommented
                    // mailService.sendAvailabilityDeleted(therapist.getEmail(),tr);
                });
            });
        }

        request.setArchived(true);

        availabilityRequestRepository.save(request);

    }


    /**
     * Add request time information for therapist
     *
     * @param requestId   id of request item
     * @param therapistId id of therapist in request to add request time details
     * @param dayRecords  array of time records
     */
    @RequestMapping(value = "/availability/request/{requestId}/therapists/{therapistId}", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK, reason = "Requests was added")
    @Secured("ROLE_AVAILABILITY")
    public void addRequestInfo(@PathVariable("requestId") Long requestId,
                               @PathVariable("therapistId") Long therapistId,
                               @RequestBody AvTherapistDayRecord[] dayRecords) {

        AvRequest request = availabilityRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("No such request"));

        boolean hasTherapist = request.getTherapistsRequests().stream().anyMatch(r -> therapistId.equals(r.getTherapistId()));

        if (!hasTherapist) {
            throw new NotFoundException("No such therapist in request");
        }

        List<AvTherapistDayRecord> dayRecordList = Arrays.asList(dayRecords);

        dayRecordList.forEach(day -> validateAvailTherapistDayRecord(day));

        Therapist therapist = therapistRepository.findById(therapistId)
                .orElseThrow(() -> new NotFoundException("No such therapist"));

        dayRecordList.forEach(day -> {
            Optional<AvTherapistDayRecord> record = availabilityTherapistDayRecordRepository.findByDate(therapistId, day.getDate());

            if (record.isPresent()) {
                final AvTherapistDayRecord realRecord = record.get();

                if (CollectionUtils.isEmpty(day.getTimeItems())) {
                    // remove record with no timeItems
                    availabilityTherapistDayRecordRepository.delete(realRecord);
                } else {
                    realRecord.setTimeItems(day.getTimeItems());

                    availabilityTherapistDayRecordRepository.save(realRecord);
                }
            } else {
                day.getTimeItems().forEach(time -> time.setTherapistDayRecord(day));
                day.setTherapist(therapist);
                availabilityTherapistDayRecordRepository.save(day);
            }
        });
    }

    /**
     * Send emails to all therapists in request
     *
     * @param requestId id of request item
     */
    @RequestMapping(value = "/availability/request/{requestId}/emails", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK, reason = "Emails were sent")
    @Secured("ROLE_AVAILABILITY")
    public void addNew(@PathVariable("requestId") Long requestId) {

        AvRequest request = availabilityRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("No such request"));

        List<AvTherapistRequest> therapistRequests = request.getTherapistsRequests();

        if (!isNullOrEmpty(therapistRequests)) {
            therapistRequests.forEach(tr -> {
                therapistRepository.findById(tr.getTherapistId())
                        .ifPresent(therapist -> {
                            taskManager.submitTask(() -> {
//                                sendEmailToTherapist(therapist.getEmail(), therapistEmail);
                            });
                        });
            });
        }
    }


    /**
     * Approve request
     *
     * @param requestId id of request
     */
    @RequestMapping(value = "/availability/request/{requestId}/approve", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK, reason = "Approved")
    @Secured("ROLE_THERAPIST")
    public void availRequestApprove(@PathVariable("requestId") Long requestId) {

        availabilityRequestHandler(requestId, request -> {
            request.setStatus(AvTherapistRequestStatus.approved);
            return request;
        });
    }

    /**
     * Decline request
     *
     * @param requestId id of request
     */
    @RequestMapping(value = "/availability/request/{requestId}/decline", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK, reason = "Declined")
    @Secured("ROLE_THERAPIST")
    public void availRequestDecline(@PathVariable("requestId") Long requestId,
                                    @RequestBody MessageRequest messageRequest) {

        availabilityRequestHandler(requestId, request -> {
            request.setStatus(AvTherapistRequestStatus.rejected);
            request.setMessage(messageRequest.getMessage());
            return request;
        });
    }

    /**
     * Mark request as viewed
     *
     * @param requestId id of request
     */
    @RequestMapping(value = "/availability/request/{requestId}/view", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK, reason = "Viewed")
    @Secured("ROLE_THERAPIST")
    public void availRequestView(@PathVariable("requestId") Long requestId) {

        availabilityRequestHandler(requestId, request -> {
            request.setStatus(AvTherapistRequestStatus.viewed);
            return request;
        });
    }


    private void availabilityRequestHandler(Long requestId, Function<AvTherapistRequest, AvTherapistRequest> mapper) {
        AvRequest avRequest = availabilityRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("No such ava request"));

        Therapist therapist = therapistRepository.findByEmail(getCurrentUser().getEmail())
                .orElseThrow(() -> new BadRequestException("User is not a therapist"));

        Long theTherapistId = therapist.getId();
        Optional<AvTherapistRequest> therapistRequestOp = avRequest.getTherapistsRequests()
                .stream()
                .filter(avTherapistRequest -> avTherapistRequest.getTherapistId().equals(theTherapistId))
                .findFirst();

        if (!therapistRequestOp.isPresent()) {
            throw new BadRequestException("This availability request doesn't correspond to you");
        }

        therapistRequestOp.ifPresent(request -> {
            availabilityTherapistRequestRepository.save(mapper.apply(request));
        });

    }

}
