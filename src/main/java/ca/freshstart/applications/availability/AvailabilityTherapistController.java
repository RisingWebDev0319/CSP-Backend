package ca.freshstart.applications.availability;

import ca.freshstart.types.AbstractController;
import ca.freshstart.data.therapist.entity.Therapist;
import ca.freshstart.data.availability.entity.AvHistoryRecord;
import ca.freshstart.data.availability.entity.AvHistoryState;
import ca.freshstart.data.availability.entity.AvTherapistDayRecord;
import ca.freshstart.data.availability.entity.AvTimeRecord;
import ca.freshstart.exceptions.BadRequestException;
import ca.freshstart.exceptions.NotFoundException;
import ca.freshstart.types.Count;
import ca.freshstart.data.therapist.repository.TherapistRepository;
import ca.freshstart.data.availability.repository.AvHistoryRepository;
import ca.freshstart.data.availability.repository.AvTherapistDayRecordRepository;
import ca.freshstart.types.Constants;
import ca.freshstart.helpers.CspUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static ca.freshstart.helpers.DateUtils.isDatesFromToValid;
import static ca.freshstart.helpers.DateUtils.isLessDaysBetween;
import static ca.freshstart.helpers.ModelValidation.validateAvailTherapistDayRecord;

@RestController
@RequiredArgsConstructor
public class AvailabilityTherapistController extends AbstractController {

    private final AvTherapistDayRecordRepository availabilityTherapistDayRecordRepository;
    private final AvHistoryRepository availabilityHistoryRepository;
    private final TherapistRepository therapistRepository;

    /**
     * Return list of all current requests for availability
     *
     * @param therapistId id of therapist in request to add request time details
     * @param pageId      page to show
     * @param pageSize    Item on page to show (10 by default)
     * @param sort        Field to sort by, +/- prefix defines order (ACS/DESC)
     * @param dateFrom    start of date range to get history for
     * @param dateTo      end of date range to get history for
     * @return Requested list of history items
     */
    @RequestMapping(value = "/availability/therapist/{therapistId}/history", method = RequestMethod.GET)
    @Secured("ROLE_AVAILABILITY")
    public Collection<AvHistoryRecord> list(@PathVariable("therapistId") Long therapistId,
                                            @RequestParam("dateFrom") @DateTimeFormat(pattern = Constants.DATE_FORMAT) Date dateFrom,
                                            @RequestParam("dateTo") @DateTimeFormat(pattern = Constants.DATE_FORMAT) Date dateTo,
                                            @RequestParam(value = "pageId", required = false, defaultValue = "1") int pageId,
                                            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                                            @RequestParam(value = "sort", required = false) String sort) {

        if (!isDatesFromToValid(dateFrom, dateTo)) {
            throw new BadRequestException("DateFrom should be before or equal to DateTo");
        }

        PageRequest pageable = new PageRequest(pageId - 1, pageSize, CspUtils.createSort(sort));

        return availabilityHistoryRepository.findAll(pageable, therapistId, dateFrom, dateTo);
    }

    /**
     * Return count of history counts for availability records
     *
     * @param therapistId id of therapist in request to add request time details
     * @param dateFrom    start of date range to get history for
     * @param dateTo      end of date range to get history for
     * @return Requested list of history items
     */
    @RequestMapping(value = "/availability/therapist/{therapistId}/history/count", method = RequestMethod.GET)
    @Secured("ROLE_AVAILABILITY")
    public Count count(@PathVariable("therapistId") Long therapistId,
                       @RequestParam("dateFrom") @DateTimeFormat(pattern = Constants.DATE_FORMAT) Date dateFrom,
                       @RequestParam("dateTo") @DateTimeFormat(pattern = Constants.DATE_FORMAT) Date dateTo) {

        if (!isDatesFromToValid(dateFrom, dateTo)) {
            throw new BadRequestException("DateFrom should be before or equal to DateTo");
        }

        return new Count(availabilityHistoryRepository.count(therapistId, dateFrom, dateTo));
    }

    /**
     * Get availability for certain date
     *
     * @param therapistId id of therapist in request to add request time details
     * @param dateFrom    start of date range to get availability for
     * @param dateTo      end of date range to get availability for, not far than 2 weeks from dateFrom
     * @return array of availability records
     */
    @RequestMapping(value = "/availability/therapist/{therapistId}", method = RequestMethod.GET)
    @Secured("ROLE_AVAILABILITY")
    public Collection<AvTherapistDayRecord> list(@PathVariable("therapistId") Long therapistId,
                                                 @RequestParam("dateFrom") @DateTimeFormat(pattern = Constants.DATE_FORMAT) Date dateFrom,
                                                 @RequestParam("dateTo") @DateTimeFormat(pattern = Constants.DATE_FORMAT) Date dateTo) {

        if (!isDatesFromToValid(dateFrom, dateTo)) {
            throw new BadRequestException("DateFrom should be before or equal to DateTo");
        }

//        if (!isLessDaysBetween(dateFrom, dateTo, 14)) {
//            throw new BadRequestException("Date to should be not far then 2 weeks from Date from");
//        }

        therapistRepository.findById(therapistId)
                .orElseThrow(() -> new NotFoundException("No such therapist"));

        return availabilityTherapistDayRecordRepository
                .findBetweenDates(therapistId, dateFrom, dateTo);
    }

    @RequestMapping(value = "/availability/therapist/mine", method = RequestMethod.GET)
    @Secured("ROLE_THERAPIST")
    public Collection<AvTherapistDayRecord> list(@RequestParam("dateFrom") @DateTimeFormat(pattern = Constants.DATE_FORMAT) Date dateFrom,
                                                 @RequestParam("dateTo") @DateTimeFormat(pattern = Constants.DATE_FORMAT) Date dateTo) {

        if (!isDatesFromToValid(dateFrom, dateTo)) {
            throw new BadRequestException("DateFrom should be before or equal to DateTo");
        }

        if (!isLessDaysBetween(dateFrom, dateTo, 14)) {
            throw new BadRequestException("Date to should be not far then 2 weeks from Date from");
        }

        Therapist therapist = therapistRepository.findByEmail(getCurrentUser().getEmail())
                .orElseThrow(() -> new BadRequestException("User is not a therapist"));

        // requests or entire object with dates ??

        return availabilityTherapistDayRecordRepository
                .findBetweenDates(therapist.getId(), dateFrom, dateTo);
    }

    /**
     * Return detail of history record
     *
     * @param recordId id of record
     * @return array of day items of requests
     */
    @RequestMapping(value = "/availability/history/{recordId}", method = RequestMethod.GET)
    @Secured("ROLE_AVAILABILITY")
    public AvHistoryRecord getById(@PathVariable("recordId") Long recordId) {
        return availabilityHistoryRepository.findById(recordId)
                .orElseThrow(() -> new NotFoundException("No such history record"));
    }

    /**
     * Make direct changes in availability of therapist, this is logged as history item
     *
     * @param therapistId id of therapist in request to add request time details
     * @param dayRecords  items by days that will changes availability
     */
    @RequestMapping(value = "/availability/therapist/{therapistId}", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK, reason = "Updated")
    @Secured("ROLE_AVAILABILITY")
    @Transactional
    public void changeAvTherapist(@PathVariable("therapistId") Long therapistId,
                                  @RequestBody AvTherapistDayRecord[] dayRecords) {

        List<AvTherapistDayRecord> dayRecordList = Arrays.asList(dayRecords);

        Therapist therapist = therapistRepository.findById(therapistId)
                .orElseThrow(() -> new NotFoundException("No such therapist"));

        dayRecordList.forEach(dayNew -> {

            validateAvailTherapistDayRecord(dayNew);

            Optional<AvTherapistDayRecord> record = availabilityTherapistDayRecordRepository.findByDate(therapistId, dayNew.getDate());

            if (record.isPresent()) {
                final AvTherapistDayRecord dayPrev = record.get();


                CspUtils.CollectionDiffResult<AvTimeRecord, AvTimeRecord> diff = CspUtils.diffCollections(
                        dayPrev.getTimeItems(),
                        dayNew.getTimeItems(),
                        (tr1, tr2) -> tr1.getId().equals(tr2.getId()));

                List<AvTimeRecord> removed = diff.getRemoved();
                List<AvHistoryRecord> historyRemovedRecords = removed.stream()
                        .map(removedTimeRecord -> generateHistoryRecord(therapistId, dayNew.getDate(), removedTimeRecord, null))
                        .collect(Collectors.toList());
                availabilityHistoryRepository.save(historyRemovedRecords);

                List<AvTimeRecord> added = diff.getAdded();
                List<AvHistoryRecord> historyAddedRecords = added.stream()
                        .map(addedTimeRecord -> generateHistoryRecord(therapistId, dayNew.getDate(), null, addedTimeRecord))
                        .collect(Collectors.toList());
                availabilityHistoryRepository.save(historyAddedRecords);

                List<AvTimeRecord> notMoved = diff.getNotMoved();
                notMoved.forEach(notMovedPrevTimeRecord -> {
                    Long id = notMovedPrevTimeRecord.getId();
                    dayNew.getTimeItems().stream()
                            .filter(timeRecord -> timeRecord.getId().equals(id)).findFirst()
                            .ifPresent(newTimeRecord -> {
                                if (!ifTimeRecordEquals(notMovedPrevTimeRecord, newTimeRecord)) {
                                    AvHistoryRecord historyRecord = generateHistoryRecord(therapistId, dayNew.getDate(), notMovedPrevTimeRecord, newTimeRecord);
                                    availabilityHistoryRepository.save(historyRecord);
                                }
                            });
                });

                if (CollectionUtils.isEmpty(dayNew.getTimeItems())) {
                    // remove record with no timeItems
                    availabilityTherapistDayRecordRepository.delete(dayPrev);
                } else {
                    dayPrev.setTimeItems(dayNew.getTimeItems());

                    availabilityTherapistDayRecordRepository.save(dayPrev);
                }
            } else {
                // add all
                dayNew.getTimeItems().forEach(time -> {
                    time.setId(null);
                    time.setTherapistDayRecord(dayNew);
                });
                dayNew.setTherapist(therapist);
                availabilityTherapistDayRecordRepository.save(dayNew);

                // Log history record
                List<AvHistoryRecord> historyRecords = dayNew.getTimeItems().stream()
                        .map(addedTimeRecord -> generateHistoryRecord(therapistId, dayNew.getDate(), null, addedTimeRecord))
                        .collect(Collectors.toList());
                availabilityHistoryRepository.save(historyRecords);
            }
        });

    }

    private boolean ifTimeRecordEquals(AvTimeRecord prevTr,
                                       AvTimeRecord newTr) {
        return CspUtils.isObjectsEquals(prevTr.getType(), newTr.getType())
                && CspUtils.isObjectsEquals(prevTr.getTimeStart(), newTr.getTimeStart())
                && CspUtils.isObjectsEquals(prevTr.getTimeEnd(), newTr.getTimeEnd());
    }


    private AvHistoryRecord generateHistoryRecord(Long therapistId,
                                                  Date date,
                                                  AvTimeRecord prevTimeRecord,
                                                  AvTimeRecord newTimeRecord) {
        return new AvHistoryRecord(
                therapistId,
                new Date(),
                prevTimeRecord == null ? null : new AvHistoryState(date, prevTimeRecord),
                newTimeRecord == null ? null : new AvHistoryState(date, newTimeRecord),
                getCurrentUser()
        );
    }
}
