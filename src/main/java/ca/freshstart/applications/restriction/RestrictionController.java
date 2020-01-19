package ca.freshstart.applications.restriction;

import ca.freshstart.types.AbstractController;
import ca.freshstart.data.restriction.entity.Restriction;
import ca.freshstart.data.room.entity.Room;
import ca.freshstart.exceptions.ConflictException;
import ca.freshstart.exceptions.NotFoundException;
import ca.freshstart.types.Count;
import ca.freshstart.types.IdResponse;
import ca.freshstart.data.equipment.repository.EquipmentRepository;
import ca.freshstart.data.restriction.repository.RestrictionRepository;
import ca.freshstart.data.room.repository.RoomRepository;
import ca.freshstart.helpers.CspUtils;
import ca.freshstart.helpers.ModelValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class RestrictionController extends AbstractController {
    @Autowired
    private RestrictionRepository restrictionRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private EquipmentRepository equipmentRepository;

    /**
     *  Return list of restrictions
     * @param pageId page to show
     * @param pageSize Item on page to show (10 by default)
     * @param sort Field to sort by, +/- prefix defines order (ACS/DESC)
     * @return Requested restrictions
     */
    @RequestMapping(value = "/restrictions", method = RequestMethod.GET)
    @Secured("ROLE_SETTINGS")
    public Collection<Restriction> getList(@RequestParam(value = "pageId", required = false, defaultValue = "1") int pageId,
                                         @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                                         @RequestParam(value = "sort",required = false) String sort) {

        List<Restriction> list = new ArrayList<>();

        PageRequest pageRequest = new PageRequest(pageId - 1, pageSize, CspUtils.createSort(sort));

        restrictionRepository.findAll(pageRequest).forEach(list::add);

        return list;
    }

    /**
     * Return count of restrictions
     * @return Count of restrictions
     */
    @RequestMapping(value = "/restrictions/count", method = RequestMethod.GET)
    @Secured("ROLE_SETTINGS")
    public Count therapistsCount() {
        return new Count(restrictionRepository.count());
    }

    /**
     * Add new restriction
     * @param restriction restriction to add
     * @return Id of the restriction
     */
    @RequestMapping(value = "/restriction", method = RequestMethod.POST)
    @Secured("ROLE_SETTINGS")
    public IdResponse addNew(@RequestBody Restriction restriction) {
        ModelValidation.validateRestriction(restriction);

        try {
            restrictionRepository.save(restriction);
        } catch(DataIntegrityViolationException ex) {
            throw new ConflictException("Such restriction already exists");
        }

        return new IdResponse(restriction.getId());
    }

    /**
     * Return information about one restriction
     * @param restrictionId id of restriction
     * @return Restriction data
     */
    @RequestMapping(value = "/restriction/{restrictionId}", method = RequestMethod.GET)
    @Secured("ROLE_SETTINGS")
    public Restriction getById(@PathVariable("restrictionId") Long restrictionId) {
        return restrictionRepository.findById(restrictionId)
               .orElseThrow(() -> new NotFoundException("No such restriction"));
    }

    /**
     * Update restriction
     * @param restrictionId id of restriction
     * @param restrictionFrom Updated restriction
     */
    @RequestMapping(value = "/restriction/{restrictionId}", method = RequestMethod.PUT)
    @ResponseStatus(value= HttpStatus.OK, reason = "Updated")
    @Secured("ROLE_SETTINGS")
    public void update(@PathVariable("restrictionId") Long restrictionId, @RequestBody Restriction restrictionFrom) {
        ModelValidation.validateRestriction(restrictionFrom);

        Restriction restriction = restrictionRepository.findById(restrictionId)
                    .orElseThrow(() -> new NotFoundException("No such restriction"));

        restriction.setName(restrictionFrom.getName());
        restriction.setLinkedId(restrictionFrom.getLinkedId());
        restriction.setType(restrictionFrom.getType());

        try {
            restrictionRepository.save(restriction);
        } catch(DataIntegrityViolationException ex) {
            throw new ConflictException("Such restriction already exists");
        }
    }

    /**
     * Delete restriction
     * @param restrictionId id of restriction
     */
    @RequestMapping(value = "/restriction/{restrictionId}", method = RequestMethod.DELETE)
    @ResponseStatus(value= HttpStatus.OK, reason = "Deleted")
    @Secured("ROLE_SETTINGS")
    public void deleteById(@PathVariable("restrictionId") Long restrictionId) {
        Restriction restriction = restrictionRepository.findById(restrictionId)
                    .orElseThrow(() -> new NotFoundException("No such restriction"));

        restrictionRepository.delete(restriction);
    }

    /**
     * Set rooms for restriction
     * @param restrictionId id of restriction
     * @param rooms array of event ids
     */
    @RequestMapping(value = "/restriction/{restrictionId}/rooms", method = RequestMethod.PUT)
    @ResponseStatus(value= HttpStatus.OK, reason = "Updated")
    @Secured("ROLE_SETTINGS")
    public void updateRooms(@PathVariable("restrictionId") Long restrictionId, @RequestBody long[] rooms) {
        Restriction restriction = restrictionRepository.findById(restrictionId)
                    .orElseThrow(() -> new NotFoundException("No such restriction"));

        if (rooms.length == 0) {
            restriction.setRooms(new HashSet<>());
        } else {
            List<Long> ids = Arrays.stream(rooms).boxed().collect(Collectors.toList());
            List<Room> roomList = roomRepository.findByIds(ids);
            restriction.setRooms(new HashSet<>(roomList));
        }

        restrictionRepository.save(restriction);
    }

    /**
     * Set equipments for restriction
     * @param restrictionId id of restriction
     * @param equipments array of equipment ids
     */
    @RequestMapping(value = "/restriction/{restrictionId}/equipments", method = RequestMethod.PUT)
    @ResponseStatus(value= HttpStatus.OK, reason = "Updated")
    @Secured("ROLE_SETTINGS")
    public void updateEquipments(@PathVariable("restrictionId") Long restrictionId, @RequestBody long[] equipments) {
        Restriction restriction = restrictionRepository.findById(restrictionId)
                    .orElseThrow(() -> new NotFoundException("No such restriction"));

        if (equipments.length == 0) {
            restriction.setEquipments(new HashSet<>());
        } else {
            restriction.setEquipments(equipmentRepository.findByIds(Arrays.stream(equipments).boxed().collect(Collectors.toList())));
        }

        restrictionRepository.save(restriction);
    }
}