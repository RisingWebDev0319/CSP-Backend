package ca.freshstart.applications.therapist;

import ca.freshstart.data.therapist.entity.TherapistInfo;
import ca.freshstart.helpers.ModelValidation;
import ca.freshstart.types.*;
import ca.freshstart.data.appUser.entity.AppUser;
import ca.freshstart.data.event.entity.Event;
import ca.freshstart.data.event.repository.EventRepository;
import ca.freshstart.data.eventRecord.entity.EventRecord;
import ca.freshstart.data.eventRecord.repository.EventRecordRepository;
import ca.freshstart.data.room.entity.Room;
import ca.freshstart.data.room.repository.RoomRepository;
import ca.freshstart.data.service.entity.Service;
import ca.freshstart.data.service.repository.ServiceRepository;
import ca.freshstart.data.serviceCategory.entity.ServiceCategory;
import ca.freshstart.data.availability.entity.AvRequest;
import ca.freshstart.data.serviceCategory.repository.ServiceCategoryRepository;
import ca.freshstart.data.therapist.entity.Therapist;
import ca.freshstart.data.therapist.repository.TherapistRepository;
import ca.freshstart.data.therapist.repository.TherapistInfoRepository;
import ca.freshstart.exceptions.BadRequestException;
import ca.freshstart.exceptions.ConflictException;
import ca.freshstart.exceptions.NotFoundException;
import ca.freshstart.data.availability.repository.AvRequestRepository;
import ca.freshstart.helpers.CspUtils;
import ca.freshstart.helpers.PasswordEncryptor;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class TherapistController extends AbstractController {

    private final TherapistRepository therapistRepository;
    private final ServiceRepository serviceRepository;
    private final ServiceCategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final EventRecordRepository eventRecordRepository;
    private final RoomRepository roomRepository;
    private final AvRequestRepository avRequestRepository;
    private final TherapistInfoRepository therapistInfoRepository;

    /**
     * Return list of therapists
     *
     * @param pageId   page to show
     * @param pageSize Item on page to show (10 by default)
     * @param sort     Field to sort by, +/- prefix defines order (ACS/DESC)
     * @return Requested therapists
     */
    @RequestMapping(value = "/therapists", method = RequestMethod.GET)
    @Secured({"ROLE_SETTINGS", "ROLE_SESSIONS", "ROLE_THERAPIST"})
    public Collection<Therapist> therapists(
            @RequestParam(value = "pageId", required = false, defaultValue = "1") int pageId,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
            @RequestParam(value = "sort", required = false) String sort) {

        return therapistRepository.findAllAsList(new PageRequest(pageId - 1, pageSize, CspUtils.createSort(sort)));
    }

    /**
     * Return count of therapists
     *
     * @return Count of therapists
     */
    @RequestMapping(value = "/therapists/count", method = RequestMethod.GET)
    @Secured({"ROLE_SETTINGS", "ROLE_SESSIONS", "ROLE_THERAPIST"})
    public Count therapistsCount() {
        return new Count(therapistRepository.count());
    }

    /**
     * Return list of therapists that has not linked to services or service categories
     *
     * @param pageId   page to show
     * @param pageSize Item on page to show (10 by default)
     * @param sort     Field to sort by, +/- prefix defines order (ACS/DESC)
     * @return Requested therapists
     */
    @RequestMapping(value = "/therapists/unassigned", method = RequestMethod.GET)
    @Secured({"ROLE_SETTINGS", "ROLE_SESSIONS"})
    public Collection<Therapist> therapistsUnassigned(
            @RequestParam(value = "pageId", required = false, defaultValue = "1") int pageId,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
            @RequestParam(value = "sort", required = false) String sort) {

        return therapistRepository.findAllUnassigned(new PageRequest(pageId - 1, pageSize, CspUtils.createSort(sort)));
    }

    /**
     * Return count of unassigned therapists
     *
     * @return Count of therapists
     */
    @RequestMapping(value = "/therapists/unassigned/count", method = RequestMethod.GET)
    @Secured({"ROLE_SETTINGS", "ROLE_SESSIONS"})
    public Count therapistsCountUnassigned() {

        return new Count(therapistRepository.countUnassigned());
    }

    /**
     * Return information about one therapist
     *
     * @param therapistId id of therapist
     * @return Therapist data
     */
    @RequestMapping(value = "/therapist/{therapistId}", method = RequestMethod.GET)
    @Secured({"ROLE_SETTINGS", "ROLE_SESSIONS"})
    public Therapist getUserById(@PathVariable("therapistId") Long therapistId) {

        return therapistRepository.findById(therapistId)
                .orElseThrow(() -> new NotFoundException("No such therapist"));
    }

    /**
     * Set services for therapist
     *
     * @param therapistId  id of therapist
     * @param serviceIdArr array of service ids
     */
    @RequestMapping(value = "/therapist/{therapistId}/services", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK, reason = "Update done")
    @Secured("ROLE_SETTINGS")
    public void updateServices(@PathVariable("therapistId") Long therapistId, @RequestBody long[] serviceIdArr) {
        Therapist therapist = therapistRepository.findById(therapistId)
                .orElseThrow(() -> new NotFoundException("No such therapist"));

        List<Long> serviceIdList = Arrays.stream(serviceIdArr).boxed().collect(Collectors.toList());
        if (serviceIdList.isEmpty()) {
            therapist.setServices(new HashSet<>());
        } else {
            List<Service> services = serviceRepository.findByIds(serviceIdList);
            therapist.setServices(new HashSet<>(services));
        }
        therapistRepository.save(therapist);
    }

    /**
     * Set categories for therapist
     *
     * @param therapistId   id of therapist
     * @param categoryIdArr array of service ids
     */
    @RequestMapping(value = "/therapist/{therapistId}/categories", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK, reason = "Update done")
    @Secured("ROLE_SETTINGS")
    public void updateCategories(@PathVariable("therapistId") Long therapistId, @RequestBody long[] categoryIdArr) {
        Therapist therapist = therapistRepository.findById(therapistId)
                .orElseThrow(() -> new NotFoundException("No such therapist"));

        List<Long> categoryIdList = Arrays.stream(categoryIdArr).boxed().collect(Collectors.toList());
        if (categoryIdList.isEmpty()) {
            therapist.setServiceCategories(new HashSet<>());
        } else {
            List<ServiceCategory> categories = categoryRepository.findByIds(categoryIdList);
            therapist.setServiceCategories(new HashSet<>(categories));
        }
        therapistRepository.save(therapist);
    }

    /**
     * Set events for therapist
     *
     * @param therapistId id of therapist
     * @param events      array of events
     */
    @RequestMapping(value = "/therapist/{therapistId}/events", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK, reason = "Update done")
    @Secured("ROLE_SETTINGS")
    public void updateEvents(@PathVariable("therapistId") Long therapistId, @RequestBody EventRequest[] events) {
        Therapist therapist = therapistRepository.findById(therapistId)
                .orElseThrow(() -> new NotFoundException("No such therapist"));

        Map<Long, EventRequest> map = Arrays.stream(events).collect(Collectors.toMap(EventRequest::getEventId, e -> e));

        Set<Long> keys = map.keySet();
        if (keys.isEmpty()) {
            therapist.setEvents(new HashSet<>());
        } else {
            List<Event> eventList = eventRepository.findByIds(keys);

            Set<EventRecord> eventRecordList = eventList.stream()
                    .map(event -> new EventRecord(map.get(event.getId()).getCapacity(), event))
                    .collect(Collectors.toSet());

            eventRecordRepository.save(eventRecordList);
            therapist.setEvents(eventRecordList);
        }

        therapistRepository.save(therapist);
    }

    /**
     * Set rooms for therapist
     *
     * @param therapistId id of therapist
     * @param roomIdArr   array of room ids
     */
    @RequestMapping(value = "/therapist/{therapistId}/rooms", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK, reason = "Update done")
    @Secured("ROLE_SETTINGS")
    public void updateRooms(@PathVariable("therapistId") Long therapistId,
                            @RequestBody long[] roomIdArr) {
        Therapist therapist = therapistRepository.findById(therapistId)
                .orElseThrow(() -> new NotFoundException("No such Therapist"));

        List<Long> roomIdList = Arrays.stream(roomIdArr).boxed().collect(Collectors.toList());

        if (!roomIdList.isEmpty()) {
            List<Room> rooms = roomRepository.findByIds(roomIdList);
            therapist.setPreferredRooms(new HashSet<>(rooms));
            therapistRepository.save(therapist);
        }
    }

    // --------------------

    /**
     * Return all therapists that has not linked therapists account
     *
     * @param pageId   page to show
     * @param pageSize Item on page to show (10 by default)
     * @param sort     Field to sort by, +/- prefix defines order (ACS/DESC)
     * @return Requested therapists
     */
    @RequestMapping(value = "/therapists/noaccount", method = RequestMethod.GET)
    @Secured({"ROLE_SETTINGS"})
    public Collection<Therapist> therapistsNoAccount(
            @RequestParam(value = "pageId", required = false, defaultValue = "1") int pageId,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
            @RequestParam(value = "sort", required = false) String sort) {

        return therapistRepository.findAllNoAccount(new PageRequest(pageId - 1, pageSize, CspUtils.createSort(sort)));
    }

    /**
     * Return count of therapists without account
     *
     * @return Count of therapists
     */
    @RequestMapping(value = "/therapists/noaccount/count", method = RequestMethod.GET)
    @Secured({"ROLE_SETTINGS"})
    public Count therapistsCountNoAccount() {

        return new Count(therapistRepository.countNoAccount());
    }

    /**
     * Create account for therapist and send email with credentials
     *
     * @param therapistId id of request
     */
    @RequestMapping(value = "/therapist/{therapistId}/account", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK, reason = "Created")
    @Secured({"ROLE_SETTINGS"})
    public void newTherapistUser(@PathVariable("therapistId") Long therapistId) {

        Therapist therapist = therapistRepository.findById(therapistId)
                .orElseThrow(() -> new NotFoundException("No such therapist"));

        AppUser user = new AppUser();

        user.setEmail(therapist.getEmail());
        user.setName(therapist.getName());
        user.setModules(Arrays.asList(Module.THERAPIST.name(), Module.AVAILABILITY.name()));

        String plainPassword = "password-" + (100 + new Random().nextInt(899));
        user.setPassword(plainPassword);

        try {
            user.setPassword(PasswordEncryptor.encryptPassword(user.getPassword()));
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Account already exists");
        }

        mailService.sendEmail("FreshStart account", user.getEmail(), "Password for your account: " + plainPassword);
    }

    // --------------------

    /**
     * Return requests for current user
     *
     * @return Return all requests for current user
     */
    @RequestMapping(value = "/availability/requests/mine", method = RequestMethod.GET)
    @Secured({"ROLE_THERAPIST"})
    public Collection<AvRequest> availRequestsMine(@RequestParam(value = "pageId", required = false, defaultValue = "1") int pageId,
                                                   @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                                                   @RequestParam(value = "sort", required = false) String sort) {

        AppUser user = getCurrentUser();

        Therapist therapist = therapistRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new BadRequestException("User is not a therapist"));

        PageRequest pageRequest = new PageRequest(pageId - 1, pageSize, CspUtils.createSort(sort));

        return avRequestRepository.findRequestsByTherapistId(therapist.getId(), pageRequest);
    }

    @RequestMapping(value = "/availability/requests/mine/count", method = RequestMethod.GET)
    @Secured("ROLE_AVAILABILITY")
    public Count availRequestsMineCount() {
        AppUser user = getCurrentUser();

        Therapist therapist = therapistRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new BadRequestException("User is not a therapist"));

        return new Count(avRequestRepository.countRequestsByTherapistId(therapist.getId()));
    }

    /**
     * @param {TherapistInfo} data
     * @return {IdResponse}
     */
    @RequestMapping(value = "/therapistInfo", method = RequestMethod.POST)
    public IdResponse createTherapistInfo(TherapistInfo data) {
        ModelValidation.validateTherapistInfo(data);
        try {
            data = therapistInfoRepository.save(data);
        } catch (DataIntegrityViolationException ex) {
            new NotFoundException(String.format("therapistInfo record failed"));
        }
        return new IdResponse(data.getId());
    }

    @RequestMapping(value = "/therapist/checkEmail", method = RequestMethod.POST)
    @Secured("ROLE_SETTINGS")
    public boolean isUniqueEmail(@RequestBody String email) {
        return !therapistRepository.existsTherapistByEmail(email);
    }

    @RequestMapping(value = "/therapist", method = RequestMethod.POST)
    @Secured("ROLE_SETTINGS")
    public IdResponse createTherapist(@RequestBody Therapist data) {
        try {
            Therapist record = new Therapist();
            ModelValidation.validateTherapistInfo(data.getTherapistInfo());

            if (!isUniqueEmail(data.getEmail())) {
                throw new ConflictException("Therapist email is exists already");
            }

            TherapistInfo recInfo = therapistInfoRepository.save(data.getTherapistInfo());
            record.setTherapistInfo(recInfo);
            record.setEmail(data.getEmail());
            record.setName(data.getName());
            data = therapistRepository.save(record);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Error to record therapist");
        }
        return new IdResponse(data.getId());
    }

    @RequestMapping(value = "/therapist/{therapistId}", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK, reason = "Updated")
    @Secured("ROLE_SETTINGS")
    public void updateTherapist(@PathVariable("therapistId") Long therapistId, @RequestBody Therapist data) {

        Therapist record = therapistRepository.findById(therapistId)
                .orElseThrow(() -> new NotFoundException("No such therapist"));
        try {
            ModelValidation.validateTherapistInfo(data.getTherapistInfo());
            therapistInfoRepository.save(data.getTherapistInfo());

            record.setTherapistInfo(data.getTherapistInfo());
            record.setEmail(data.getEmail());
            record.setName(data.getName());
            therapistRepository.save(record);
        } catch (DataIntegrityViolationException ex) {
            throw new NotFoundException("Failed update therapist: " + ex.getMessage());
        }
    }

    @RequestMapping(value = "/therapist/{therapistId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK, reason = "Deleted")
    @Secured("ROLE_SETTINGS")
    public void deleteTherapistById(@PathVariable("therapistId") Long therapistId) {
        Therapist record = therapistRepository.findById(therapistId)
                .orElseThrow(() -> new NotFoundException("No such therapist"));
        record.setArchived(true);
        therapistRepository.save(record);
    }
}
