package ca.freshstart.applications.taxes;

import ca.freshstart.data.event.entity.Event;
import ca.freshstart.data.event.repository.EventRepository;
import ca.freshstart.data.service.entity.Service;
import ca.freshstart.data.service.repository.ServiceRepository;
import ca.freshstart.data.taxes.entity.Taxes;
import ca.freshstart.data.taxes.repository.TaxesRepository;
import ca.freshstart.exceptions.NotFoundException;
import ca.freshstart.helpers.CspUtils;
import ca.freshstart.types.AbstractController;
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
public class TaxesController extends AbstractController {
    private final ServiceRepository serviceRepository;
    private final TaxesRepository taxesRepository;
    private final EventRepository eventRepository;

    /**
     * @param {Integer} pageId
     * @param {Integer} pageSize
     * @param {String   }sort
     * @return {Collection<Taxes>}
     */
    @RequestMapping(value = "/taxes", method = RequestMethod.GET)
    @Secured({"ROLE_SETTINGS"})
    public Collection<Taxes> taxes(
            @RequestParam(value = "pageId", required = false, defaultValue = "1") int pageId,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
            @RequestParam(value = "sort", required = false) String sort) {

        return taxesRepository.findAllAsList(new PageRequest(pageId - 1, pageSize, CspUtils.createSort(sort)));
    }

    @RequestMapping(value = "/taxes/count", method = RequestMethod.GET)
    @Secured({"ROLE_SETTINGS"})
    public Count taxesCount() {
        return new Count(taxesRepository.count());
    }

    @RequestMapping(value = "/taxes/{taxId}", method = RequestMethod.GET)
    @Secured({"ROLE_SETTINGS"})
    public Taxes getTaxById(@PathVariable("taxId") Long taxId) {

        return taxesRepository.findById(taxId)
                .orElseThrow(() -> new NotFoundException("No such tax"));
    }

    @RequestMapping(value = "/taxes/byCode/{code}", method = RequestMethod.GET)
    @Secured({"ROLE_SETTINGS"})
    public List<Taxes> getTaxByCode(@PathVariable("code") String code) {
        return taxesRepository.findByCode(code);
    }

    @RequestMapping(value = "/taxes", method = RequestMethod.POST)
    @Secured("ROLE_SETTINGS")
    public IdResponse createTax(@RequestBody Taxes tax) {

        tax = taxesRepository.save(tax);
        return new IdResponse(tax.getId());
    }

    @RequestMapping(value = "/taxes/{taxId}", method = RequestMethod.PUT)
    @Secured("ROLE_SETTINGS")
    public void updateTax(@PathVariable("taxId") Long taxId, @RequestBody Taxes tax) {
        Taxes taxesBefore = taxesRepository.findById(taxId)
                .orElseThrow(() -> new NotFoundException("No such tax"));

        taxesBefore.setCode(tax.getCode());
        taxesBefore.setTitle(tax.getTitle());

        taxesRepository.save(taxesBefore);
    }

    @RequestMapping(value = "/taxes/{taxId}", method = RequestMethod.DELETE)
    @Secured("ROLE_SETTINGS")
    public void updateTax(@PathVariable("taxId") Long taxId) {
        Taxes tax = taxesRepository.findById(taxId)
                .orElseThrow(() -> new NotFoundException("No such tax"));
        tax.setArchived(true);

        List<Service> serviceList = serviceRepository.findByTaxId(taxId);
        if (serviceList.size() > 0) {
            serviceList.stream().map(service -> {
                        service.setTax(null);
                        return service;
                    }
            )
                    .collect(Collectors.toList());
            serviceRepository.save(serviceList);
        }

        List<Event> eventsList = eventRepository.findByTaxId(taxId);
        if (eventsList.size() > 0) {
            eventsList.stream().map(event -> {
                        event.setTax(null);
                        return event;
                    }
            )
                    .collect(Collectors.toList());
            eventRepository.save(eventsList);
        }

        taxesRepository.save(tax);
    }

}
