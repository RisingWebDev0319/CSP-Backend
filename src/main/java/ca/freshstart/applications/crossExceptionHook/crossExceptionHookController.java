package ca.freshstart.applications.crossExceptionHook;

import ca.freshstart.exceptions.crossing.CrossingDataException;
import ca.freshstart.exceptions.crossing.CrossingDataListException;
import ca.freshstart.data.matching.types.CrossingData;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

@ControllerAdvice
public class crossExceptionHookController {

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(CrossingDataListException.class)
    @ResponseBody
    Collection<CrossingData>
    handleCrossingData(HttpServletRequest req, CrossingDataListException ex) {
        return ex.getCrossingDataList();
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(CrossingDataException.class)
    @ResponseBody
    CrossingData
    handleCrossingData(HttpServletRequest req, CrossingDataException ex) {
        return ex.getCrossingData();
    }

}