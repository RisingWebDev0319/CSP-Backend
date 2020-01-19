package ca.freshstart.applications.serviceCategory;

import ca.freshstart.types.AbstractController;
import ca.freshstart.data.service.entity.Service;
import ca.freshstart.data.serviceCategory.entity.ServiceCategory;
import ca.freshstart.data.therapist.entity.Therapist;
import ca.freshstart.exceptions.BadRequestException;
import ca.freshstart.exceptions.ConflictException;
import ca.freshstart.exceptions.NotFoundException;
import ca.freshstart.types.Count;
import ca.freshstart.types.IdRequest;
import ca.freshstart.types.IdResponse;
import ca.freshstart.data.serviceCategory.repository.ServiceCategoryRepository;
import ca.freshstart.data.service.repository.ServiceRepository;
import ca.freshstart.helpers.CspUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

import static ca.freshstart.helpers.CspUtils.isNullOrEmpty;
import static ca.freshstart.helpers.ModelValidation.validateConcreteCalendarEvent;
import static ca.freshstart.helpers.ModelValidation.validateServiceCategory;

@RestController
@RequiredArgsConstructor
public class ServiceCategoryController extends AbstractController {

    private final ServiceCategoryRepository serviceCategoryRepository;
    private final ServiceRepository serviceRepository;

    /**
     * Return list of serviceCategories
     *
     * @param pageId   page to show
     * @param pageSize Item on page to show (10 by default)
     * @param sort     Field to sort by, +/- prefix defines order (ACS/DESC)
     * @return Requested serviceCategories
     */
    @RequestMapping(value = "/serviceCategories", method = RequestMethod.GET)
    @Secured("ROLE_SETTINGS")
    public Collection<ServiceCategory> list(@RequestParam(value = "pageId", required = false, defaultValue = "1") int pageId,
                                            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                                            @RequestParam(value = "sort", required = false) String sort) {

        List<ServiceCategory> list = new ArrayList<>();

        PageRequest pageRequest = new PageRequest(pageId - 1, pageSize, CspUtils.createSort(sort));

        serviceCategoryRepository.findAll(pageRequest).forEach(list::add);
        return list;
    }

    /**
     * Return count of serviceCategories
     *
     * @return Count of serviceCategories
     */
    @RequestMapping(value = "/serviceCategories/count", method = RequestMethod.GET)
    @Secured("ROLE_SETTINGS")
    public Count count() {

        return new Count(serviceCategoryRepository.count());
    }

    /**
     * Add new serviceCategory
     *
     * @param serviceCategory serviceCategory to add.
     * @return Id
     */
    @RequestMapping(value = "/serviceCategory", method = RequestMethod.POST)
    @Secured("ROLE_SETTINGS")
    public IdResponse addNew(@RequestBody ServiceCategory serviceCategory) {

        validateServiceCategory(serviceCategory);

        if (serviceCategory.getName().length() > 255) {
            throw new BadRequestException("Category name is too long");
        }

        try {
            serviceCategory = serviceCategoryRepository.save(serviceCategory);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Such category already exists");
        }

        return new IdResponse(serviceCategory.getId());
    }

    /**
     * Return information about one serviceCategory
     *
     * @param serviceCategoryId id of serviceCategory
     * @return ServiceCategory data
     */
    @RequestMapping(value = "/serviceCategory/{serviceCategoryId}", method = RequestMethod.GET)
    @Secured("ROLE_SETTINGS")
    public ServiceCategory getById(@PathVariable("serviceCategoryId") Long serviceCategoryId) {

        return serviceCategoryRepository.findById(serviceCategoryId)
                .orElseThrow(() -> new NotFoundException("No such serviceCategory"));
    }

    /**
     * Update serviceCategory
     *
     * @param serviceCategoryId   id of serviceCategory
     * @param serviceCategoryFrom Updated serviceCategory
     */
    @RequestMapping(value = "/serviceCategory/{serviceCategoryId}", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK, reason = "Updated")
    @Secured("ROLE_SETTINGS")
    public void update(@PathVariable("serviceCategoryId") Long serviceCategoryId,
                       @RequestBody ServiceCategory serviceCategoryFrom) {

        validateServiceCategory(serviceCategoryFrom);

        ServiceCategory serviceCategory = serviceCategoryRepository.findById(serviceCategoryId)
                .orElseThrow(() -> new NotFoundException("No such serviceCategory"));

        serviceCategory.setName(serviceCategoryFrom.getName());

        try {
            serviceCategoryRepository.save(serviceCategory);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Such category already exists");
        }
    }

    /**
     * Delete serviceCategory
     *
     * @param serviceCategoryId id of serviceCategory
     */
    @RequestMapping(value = "/serviceCategory/{serviceCategoryId}", method = RequestMethod.DELETE)
    @Secured("ROLE_SETTINGS")
    public Collection<Therapist> delete(@PathVariable("serviceCategoryId") Long serviceCategoryId,
                                        HttpServletResponse response) {

        ServiceCategory serviceCategory = serviceCategoryRepository.findById(serviceCategoryId)
                .orElseThrow(() -> new NotFoundException("No such serviceCategory"));
        System.out.println("____+++____++" + serviceCategory + "***********\n" + serviceCategory.getTherapists());
        if (!isNullOrEmpty(serviceCategory.getTherapists())) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return serviceCategory.getTherapists();
        }

        serviceCategoryRepository.delete(serviceCategory);

        return null;
    }

    // services for categories

    /**
     * Override services assigned to category
     *
     * @param serviceCategoryId id of serviceCategory
     * @param services          array of service ids
     */
    @RequestMapping(value = "/serviceCategory/{serviceCategoryId}/services", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK, reason = "Updated")
    @Secured("ROLE_SETTINGS")
    public void addNewService(@PathVariable("serviceCategoryId") Long serviceCategoryId,
                              @RequestBody long[] services) {

        ServiceCategory serviceCategory = serviceCategoryRepository.findById(serviceCategoryId)
                .orElseThrow(() -> new NotFoundException("No such serviceCategory"));

        List<Long> ids = Arrays.stream(services).boxed().collect(Collectors.toList());
        if (!ids.isEmpty()) {
            List<Service> serviceList = serviceRepository.findByIds(ids);
            serviceCategory.setServices(new HashSet<>(serviceList));
        } else {
            serviceCategory.setServices(new HashSet<>());
        }

        serviceCategoryRepository.save(serviceCategory);
    }

    /**
     * Add service to category
     *
     * @param serviceCategoryId id of serviceCategory
     * @param serviceId         id of service
     */
    @RequestMapping(value = "/serviceCategory/{serviceCategoryId}/services", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK, reason = "Updated")
    @Secured("ROLE_SETTINGS")
    public void addService(@PathVariable("serviceCategoryId") Long serviceCategoryId,
                           @RequestBody IdRequest serviceId) {

        ServiceCategory serviceCategory = serviceCategoryRepository.findById(serviceCategoryId)
                .orElseThrow(() -> new NotFoundException("No such serviceCategory"));

        serviceCategory.getServices()
                .add(serviceRepository.findById(serviceId.getId())
                        .orElseThrow(() -> new NotFoundException("No such service")));

        serviceCategoryRepository.save(serviceCategory);
    }

    /**
     * Remove service from category
     *
     * @param serviceCategoryId id of serviceCategory
     * @param serviceId         id of service
     */
    @RequestMapping(value = "/serviceCategory/{serviceCategoryId}/services", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK, reason = "Updated")
    @Secured("ROLE_SETTINGS")
    public void deleteService(@PathVariable("serviceCategoryId") Long serviceCategoryId,
                              @RequestBody IdRequest serviceId) {

        ServiceCategory serviceCategory = serviceCategoryRepository.findById(serviceCategoryId)
                .orElseThrow(() -> new NotFoundException("No such serviceCategory"));

        serviceCategory.getServices()
                .remove(serviceRepository.findById(serviceId.getId())
                        .orElseThrow(() -> new NotFoundException("No such service")));

        serviceCategoryRepository.save(serviceCategory);
    }
}