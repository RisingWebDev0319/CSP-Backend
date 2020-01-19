package ca.freshstart.applications.suggestedService;

import ca.freshstart.types.AbstractController;
import ca.freshstart.data.suggestions.entity.SsCustomColumn;
import ca.freshstart.data.suggestions.entity.SsCustomColumnValue;
import ca.freshstart.exceptions.BadRequestException;
import ca.freshstart.exceptions.ConflictException;
import ca.freshstart.exceptions.NotFoundException;
import ca.freshstart.types.IdResponse;
import ca.freshstart.data.suggestions.repository.SsCustomColumnRepository;
import ca.freshstart.data.suggestions.repository.SsTableColumnRepository;
import ca.freshstart.helpers.ModelValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
public class SsCustomColumnController extends AbstractController {

    @Autowired
    private SsCustomColumnRepository suggestedCustomColumnRepository;

    @Autowired
    private SsTableColumnRepository suggestedColumnRepository;

    /**
     * Return all available custom columns
     * @return Requested data
     */
    @RequestMapping(value = "/suggestedServices/customColumns", method = RequestMethod.GET)
    @Secured({"ROLE_SUGGESTED_SERVICES_UI"})
    public Collection<SsCustomColumn> getAll() {
        return suggestedCustomColumnRepository.findAll();
    }

    /**
     * Add new custom column in table to show
     * @param customColumn column to add
     * @return Id of the column
     */
    @RequestMapping(value = "/suggestedServices/customColumn", method = RequestMethod.POST)
    @Secured("ROLE_SUGGESTED_SERVICES_UI")
    public IdResponse addRoom(@RequestBody SsCustomColumn customColumn) {
        ModelValidation.validateCustomColumn(customColumn);

        try {
            suggestedCustomColumnRepository.save(customColumn);
        } catch(DataIntegrityViolationException ex) {
            throw new ConflictException("Name should be unique and not empty");
        }

        return new IdResponse(customColumn.getId());
    }

    /**
     * Delete custom column and all linked data
     * @param columnId id of column
     */
    @RequestMapping(value = "/suggestedServices/customColumn/{columnId}", method = RequestMethod.DELETE)
    @ResponseStatus(value= HttpStatus.OK, reason = "Deleted")
    @Secured("ROLE_SUGGESTED_SERVICES_UI")
    public void deleteCustomColumnId(@PathVariable("columnId") Long columnId) {
        SsCustomColumn column = suggestedCustomColumnRepository.findById(columnId)
                                .orElseThrow(() -> new NotFoundException("No such custom column"));

        suggestedCustomColumnRepository.delete(column);
    }

    /**
     * Update custom column
     * @param columnId id of column
     * @param columnFrom ...
     */
    @RequestMapping(value = "/suggestedServices/customColumn/{columnId}", method = RequestMethod.PUT)
    @ResponseStatus(value= HttpStatus.OK, reason = "Updated")
    @Secured("ROLE_SUGGESTED_SERVICES_UI")
    public void updateCustomColumn(@PathVariable("columnId") Long columnId, @RequestBody SsCustomColumn columnFrom) {

        SsCustomColumn column = suggestedCustomColumnRepository.findById(columnId)
                                .orElseThrow(() -> new NotFoundException("No such custom column"));

        column.setTitle(columnFrom.getTitle());
        column.setType(columnFrom.getType());

        try {
            suggestedCustomColumnRepository.save(column);
        } catch(DataIntegrityViolationException ex) {
            throw new ConflictException("Name should be unique and not empty");
        }
    }

    /**
     * Update selectable values for column
     * @param columnId id of column
     */
    @RequestMapping(value = "/suggestedServices/customColumn/{columnId}/values", method = RequestMethod.PUT)
    @ResponseStatus(value= HttpStatus.OK, reason = "Updated")
    @Secured("ROLE_SUGGESTED_SERVICES_UI")
    public void updateCustomColumnValues(@PathVariable("columnId") Long columnId, @RequestBody SsCustomColumnValue[] values) {

        SsCustomColumn column = suggestedCustomColumnRepository.findById(columnId)
                                .orElseThrow(() -> new NotFoundException("No such custom column"));

        if(suggestedColumnRepository.findCustomColumnUsage(columnId).isPresent()) {
            throw new BadRequestException("Working column type");
        }

        Map<Long, SsCustomColumnValue> map = column.getValues().stream()
                                                .collect(Collectors.toMap(SsCustomColumnValue::getId,
                                                    Function.identity()));

        Arrays.stream(values).filter(entry -> {
            SsCustomColumnValue value = map.get(entry.getId());

            if(value != null) {
                value.setTitle(entry.getTitle());
                value.setValue(entry.getValue());
                value.setOrder(entry.getOrder());

                map.remove(entry.getId());

                return false;
            }

            return true;
        }).forEach(rest -> column.addValue(rest));

        map.forEach((k, v) -> column.removeValue(v));

        try {
            suggestedCustomColumnRepository.save(column);
        } catch(DataIntegrityViolationException ex) {
            throw new ConflictException("Name should be unique and not empty");
        }
    }
}