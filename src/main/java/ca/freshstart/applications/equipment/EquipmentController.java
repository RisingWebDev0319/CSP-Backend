package ca.freshstart.applications.equipment;

import ca.freshstart.types.AbstractController;
import ca.freshstart.data.equipment.entity.Equipment;
import ca.freshstart.data.restriction.entity.Restriction;
import ca.freshstart.exceptions.NotFoundException;
import ca.freshstart.types.Count;
import ca.freshstart.types.IdResponse;
import ca.freshstart.data.equipment.repository.EquipmentRepository;
import ca.freshstart.data.restriction.repository.RestrictionRepository;
import ca.freshstart.helpers.CspUtils;
import ca.freshstart.helpers.ModelValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class EquipmentController extends AbstractController {

    private final EquipmentRepository equipmentRepository;
    private final RestrictionRepository restrictionRepository;

    /**
     *  Return list of equipments
     * @param pageId page to show
     * @param pageSize Item on page to show (10 by default)
     * @param sort Field to sort by, +/- prefix defines order (ACS/DESC)
     * @return Requested equipments
     */
    @RequestMapping(value = "/equipments", method = RequestMethod.GET)
    @Secured("ROLE_SETTINGS")
    public Collection<Equipment> getList(@RequestParam(value = "pageId", required = false, defaultValue = "1") int pageId,
                                         @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                                         @RequestParam(value = "sort",required = false) String sort) {

        return equipmentRepository.findAllAsList(new PageRequest(pageId - 1, pageSize, CspUtils.createSort(sort)));
    }

    /**
     * Return count of equipments
     * @return Count of equipments
     */
    @RequestMapping(value = "/equipments/count", method = RequestMethod.GET)
    @Secured("ROLE_SETTINGS")
    public Count count() {
        return new Count(equipmentRepository.count());
    }

    /**
     * Add new equipment
     * @param equipment equipment to add
     * @return Id of the equipment
     */
    @RequestMapping(value = "/equipment", method = RequestMethod.POST)
    @Secured("ROLE_SETTINGS")
    public IdResponse addNew(@RequestBody Equipment equipment) {
        ModelValidation.validateEquipment(equipment);

        equipment = saveOrReplaceArchived(equipment);
        return new IdResponse(equipment.getId());
    }

    private Equipment saveOrReplaceArchived(Equipment equipment) {
        try {
            return equipmentRepository.save(equipment);
        } catch (DataIntegrityViolationException ex) {
            Equipment foundEquipment = equipmentRepository.findArchivedByName(equipment.getName())
                    .orElseThrow(() -> new NotFoundException(String.format("Equipment with the name (%s) already exist", equipment.getName())));

            foundEquipment.setName(equipment.getName());
            foundEquipment.setCapacity(equipment.getCapacity());
            foundEquipment.setArchived(false);

            return equipmentRepository.save(foundEquipment);
        }
    }


    /**
     * Return information about one equipment
     * @param equipmentId id of equipment
     * @return Equipment data
     */
    @RequestMapping(value = "/equipment/{equipmentId}", method = RequestMethod.GET)
    @Secured("ROLE_SETTINGS")
    public Equipment getById(@PathVariable("equipmentId") Long equipmentId) {

        return equipmentRepository.findById(equipmentId)
               .orElseThrow(() -> new NotFoundException("No such equipment"));
    }

    /**
     * Update equipment
     * @param equipmentId id of equipment
     * @param equipmentFrom Updated equipment
     */
    @RequestMapping(value = "/equipment/{equipmentId}", method = RequestMethod.PUT)
    @ResponseStatus(value= HttpStatus.OK, reason = "Updated")
    @Secured("ROLE_SETTINGS")
    public void update(@PathVariable("equipmentId") Long equipmentId,
                       @RequestBody Equipment equipmentFrom) {
        ModelValidation.validateEquipment(equipmentFrom);

        Equipment equipment = equipmentRepository.findById(equipmentId)
                  .orElseThrow(() -> new NotFoundException("No such equipment"));

        equipment.setName(equipmentFrom.getName());
        equipment.setCapacity(equipmentFrom.getCapacity());

        saveOrReplaceArchived(equipment);
    }

    /**
     * Delete equipment
     * @param equipmentId id of equipment
     */
    @RequestMapping(value = "/equipment/{equipmentId}", method = RequestMethod.DELETE)
    @ResponseStatus(value= HttpStatus.OK, reason = "Deleted")
    @Secured("ROLE_SETTINGS")
    public void deleteById(@PathVariable("equipmentId") Long equipmentId) {

        Equipment equipment = equipmentRepository.findById(equipmentId)
                  .orElseThrow(() -> new NotFoundException("No such equipment"));

        List<Restriction> restrictions = restrictionRepository.findByEquipmentId(equipmentId);

        restrictions.forEach(restriction -> {
            Set<Equipment> toRemove = restriction.getEquipments()
                    .stream()
                    .filter(r -> r.getId().equals(equipmentId))
                    .collect(Collectors.toSet());
            restriction.getEquipments().removeAll(toRemove);
            restrictionRepository.save(restriction);
        });

        try {
            equipmentRepository.delete(equipment);
        } catch (Exception e) {
            equipment.setArchived(true);
            equipmentRepository.save(equipment);
        }
    }
}