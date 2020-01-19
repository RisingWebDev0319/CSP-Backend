package ca.freshstart.applications.service;

import ca.freshstart.types.AbstractController;
import ca.freshstart.data.service.entity.Service;
import ca.freshstart.exceptions.NotFoundException;
import ca.freshstart.exceptions.ConflictException;
import ca.freshstart.types.Count;
import ca.freshstart.data.service.repository.ServiceRepository;
import ca.freshstart.helpers.CspUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
public class ServiceController extends AbstractController {
    @Autowired
    private ServiceRepository serviceRepository;

    /**
     * Return list of services
     *
     * @param pageId   page to show
     * @param pageSize Item on page to show (10 by default)
     * @param sort     Field to sort by, +/- prefix defines order (ACS/DESC)
     * @return Requested services
     */
    @RequestMapping(value = "/services", method = RequestMethod.GET)
    @Secured("ROLE_SETTINGS")
    public List<Service> getList(@RequestParam(value = "pageId", required = false, defaultValue = "1") int pageId,
                                       @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                                       @RequestParam(value = "sort", required = false) String sort) {
        return serviceRepository.findAllAsList(new PageRequest(pageId - 1, pageSize, CspUtils.createSort(sort)));
    }

    /**
     * Return count of services
     *
     * @return Count of services
     */
    @RequestMapping(value = "/services/count", method = RequestMethod.GET)
    @Secured("ROLE_SETTINGS")
    public Count count() {
        return new Count(serviceRepository.count());
    }

    /**
     * Return information about one service
     *
     * @param serviceId id of service
     * @return Service data
     */
    @RequestMapping(value = "/services/{serviceId}", method = RequestMethod.GET)
    @Secured("ROLE_SETTINGS")
    public Service getById(@PathVariable("serviceId") Long serviceId) {
        return serviceRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("No such service"));
    }

    @RequestMapping(value = "/services/tax/{taxId}", method = RequestMethod.GET)
    @Secured("ROLE_SESSIONS")
    public List<Service> getServicesByTaxId(@PathVariable("taxId") Long taxId) {
        return serviceRepository.findByTaxId(taxId);
    }

    /**
     * Return list of services without categories
     *
     * @param pageId   page to show
     * @param pageSize Item on page to show (10 by default)
     * @param sort     Field to sort by, +/- prefix defines order (ACS/DESC)
     * @return Requested services
     */
    @RequestMapping(value = "/services/uncategorized", method = RequestMethod.GET)
    @Secured("ROLE_SETTINGS")
    public Collection<Service> getListUncategorized(@RequestParam(value = "pageId", required = false, defaultValue = "1") int pageId,
                                                    @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                                                    @RequestParam(value = "sort", required = false) String sort) {

        return serviceRepository.findAllUncotegorized(new PageRequest(pageId - 1, pageSize, CspUtils.createSort(sort)));
    }

    /**
     * Return count of uncategorized services
     *
     * @return Count of uncategorized services
     */
    @RequestMapping(value = "/services/uncategorized/count", method = RequestMethod.GET)
    @Secured("ROLE_SETTINGS")
    public Count servicesCountUncategorized() {
        return new Count(serviceRepository.countUncategorized());
    }


    @RequestMapping(value = "/services/{serviceId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK, reason = "Deleted")
    @Secured("ROLE_SETTINGS")
    public void deleteService(@PathVariable("serviceId") Long serviceId) {
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("No such service"));
        try {
            service.setArchived(true);
            serviceRepository.save(service);
        } catch (DataIntegrityViolationException ex) {
            throw new NotFoundException("Failed delete service: " + ex.getMessage());
        }
    }

    @RequestMapping(value = "/services", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK, reason = "Created")
    @Secured("ROLE_SETTINGS")
    public void createService(@RequestBody Service body) {
        try {
            serviceRepository.save(body);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Failed create the service: " + ex.getMessage());
        }
    }

    @RequestMapping(value = "/services/{serviceId}", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK, reason = "Updated")
    @Secured("ROLE_SETTINGS")
    public void updateService(@PathVariable("serviceId") Long serviceId,
                              @RequestBody Service body) {
        serviceRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("No such service"));
        try {
            serviceRepository.save(body);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Failed create the service: " + ex.getMessage());
        }
    }

}
