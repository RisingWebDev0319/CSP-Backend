package ca.freshstart.helpers;

import ca.freshstart.data.availability.entity.AvTimeRecord;
import ca.freshstart.data.concreteEvent.entity.ConcreteEvent;
import ca.freshstart.data.concreteEvent.interfaces.BaseCrossEvent;
import ca.freshstart.data.concreteCalendarEvent.entity.ConcreteCalendarEvent;
import ca.freshstart.data.client.entity.Client;
import ca.freshstart.data.room.entity.Room;
import ca.freshstart.data.therapist.entity.Therapist;
import ca.freshstart.data.suggestions.entity.PreliminaryEvent;
import ca.freshstart.exceptions.crossing.CrossingDataException;
import ca.freshstart.data.matching.types.CrossingData;
import ca.freshstart.data.matching.types.CrossingDataItem;
import ca.freshstart.data.matching.types.CrossingDataResult;
import ca.freshstart.data.matching.types.CrossingDataType;
import ca.freshstart.types.EntityId;

import java.sql.Time;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public final class CrossingDataCalculator {

    /**
     * Check if BaseCrossEvent cross one of ConcreteEvent or ConcreteCalendarEvent or AvTimeRecord of PreliminaryEvent
     */
    public static CrossingData validateCrossing(BaseCrossEvent event, Boolean force, Long sessionId, Date date, Map<Long, Client> id2Client,
                                                List<ConcreteEvent> concreteEvents,
                                                List<ConcreteCalendarEvent> concreteCalendarEvents,
                                                List<AvTimeRecord> timeRecords,
                                                List<PreliminaryEvent> preliminaryEvents) {

        CrossingDataResult crossingDataResult = getCrossing(event, force, sessionId, date, id2Client,
                concreteEvents,
                concreteCalendarEvents,
                timeRecords,
                preliminaryEvents);

        if (crossingDataResult == null) {
            return null;
        }

        CrossingData crossingData = crossingDataResult.getCrossingData();

        if (crossingDataResult.isCriticalCrossing()) {
            throw new CrossingDataException(crossingData);
        }

        return crossingData;
    }


    private static CrossingDataResult getCrossing(BaseCrossEvent event, Boolean force, Long sessionId, Date date, Map<Long, Client> id2Client,
                                                 List<ConcreteEvent> concreteEvents,
                                                 List<ConcreteCalendarEvent> concreteCalendarEvents,
                                                 List<AvTimeRecord> timeRecords,
                                                 List<PreliminaryEvent> preliminaryEvents) {
         String dateStr = DateUtils.toDateFormat(date);

        // 1) Check crossing ConcreteEvents
          List<CrossingDataItem> crossingConcrete = CrossingDataCalculator.calculateCrossingEvents(event, concreteEvents, id2Client)
                .peek(item -> item.setType(CrossingDataType.concrete))
                .collect(toList());

        // 2) Check crossing ConcreteCalendarEvents
        List<CrossingDataItem> crossingCalendar = CrossingDataCalculator.calculateCrossingEvents(event, concreteCalendarEvents, id2Client)
                .peek(item -> item.setType(CrossingDataType.calendar))
                .collect(toList());

        // 3) Check crossing Availability
        List<CrossingDataItem> crossingUnavailable = CrossingDataCalculator.calculateCrossingDayRecord(event, timeRecords)
                .peek(item -> item.setType(CrossingDataType.unavailable))
                .collect(toList());

        // 4) Check crossing PreliminaryEvents
        List<CrossingDataItem> crossingConfirmation = CrossingDataCalculator.calculateCrossingEvents(event, preliminaryEvents, id2Client)
                .peek(item -> item.setType(CrossingDataType.confirmation))
                .collect(toList());

        List<CrossingDataItem> crossingDataItems = new ArrayList<>();
        crossingDataItems.addAll(crossingConcrete);
        crossingDataItems.addAll(crossingCalendar);
        crossingDataItems.addAll(crossingUnavailable);
        crossingDataItems.addAll(crossingConfirmation);

        if (crossingDataItems.isEmpty()) {
            return null;
        } else {
            CrossingData crossingData = new CrossingData();
            crossingData.setItems(crossingDataItems);
            crossingData.setDate(dateStr);
            crossingData.setSessionId(sessionId);

            boolean criticalCrossing = crossingConcrete.size() > 0 // crossing Concrete there - critical
                    || crossingCalendar.size() > 0 // crossing Calendar there - critical
                    || (crossingUnavailable.size() > 0 && !force) // crossing Unavailable there - optional could be forced
                    ;
            // crossing Confirmation - just as notification, could be ignored

            return new CrossingDataResult(crossingData, criticalCrossing);
        }
    }

    /**
     * Check if BaseCrossEvent cross one of AvTimeRecord
     */
    private static Stream<CrossingDataItem> calculateCrossingDayRecord(BaseCrossEvent intersectingItem,
                                                                      List<AvTimeRecord> timeRecords) {

        Time itemStart = intersectingItem.getTime();
        Time itemEnd = appendMinutes(itemStart, intersectingItem.getDuration().duration());
        Therapist therapist = intersectingItem.getTherapist();

        return timeRecords.stream()
                .map((AvTimeRecord avTimeRecord) -> {
                    Time intersectedStart = new Time(avTimeRecord.getTimeStart() * 60 * 1000);
                    Time intersectedEnd = new Time(avTimeRecord.getTimeEnd() * 60 * 1000);

                    if (isAfter(intersectedEnd, itemStart) && isBefore(intersectedStart, itemEnd)) {

                        return makeCrossingDataItem(intersectedStart, intersectedEnd,
                                itemStart, itemEnd,
                                therapist,
                                null, null);
                    } else {
                        return null;
                    }

                })
                .filter(Objects::nonNull);
    }


    private static Long getId(EntityId entity) {
        return entity == null ? null : entity.getId();
    }

    private static boolean isCrossing(Long criteriaId1, Long criteriaId2) {
        return (criteriaId1 != null && criteriaId2 != null) && criteriaId1.equals(criteriaId2);
    }

    private static List<Long> crossingIds(List<Long> criteriaIds1, List<Long> criteriaIds2) {
        if (criteriaIds1 != null && criteriaIds2 != null){
            return criteriaIds1.stream()
                    .filter(aLong -> criteriaIds2.stream().anyMatch(bLong -> bLong.equals(aLong)))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * Check if BaseCrossEvent cross one of BaseCrossEvent
     */
    private static Stream<CrossingDataItem> calculateCrossingEvents(BaseCrossEvent intersectingItem,
                                                                    List<? extends BaseCrossEvent> intersectedItems,
                                                                    Map<Long, Client> id2Client) {
        Time itemStart = intersectingItem.getTime();
        Time itemEnd = appendMinutes(itemStart, intersectingItem.getDuration().duration());
        Long therapistId = getId(intersectingItem.getTherapist());
        Long roomId = getId(intersectingItem.getRoom());
        List<Long> clientsIds = intersectingItem.getClientsIds();

        return intersectedItems.stream()
                .map((BaseCrossEvent intersectedItem) -> {
                    Time intersectedStart = intersectedItem.getTime();
                    Time intersectedEnd = appendMinutes(intersectedStart, intersectedItem.getDuration().duration());

                    if (isAfter(intersectedEnd, itemStart) && isBefore(intersectedStart, itemEnd)) {

                        Therapist intersectedTherapist = intersectedItem.getTherapist();
                        Long intersectedTherapistId = getId(intersectedTherapist);

                        Room intersectedRoom = intersectedItem.getRoom();
                        Long intersectedRoomId = getId(intersectedRoom);

                        List<Long> intersectedClientsIds = intersectedItem.getClientsIds();
                        List<Long> clientsIdsCross = CrossingDataCalculator.crossingIds(clientsIds, intersectedClientsIds);
                        List<Client> clientsCross = clientsIdsCross.stream()
                                .map(id2Client::get)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());


                        boolean therapistCross = isCrossing(therapistId, intersectedTherapistId);
                        boolean roomCross = isCrossing(roomId, intersectedRoomId);
                        if (therapistCross || roomCross || !clientsCross.isEmpty()) {

                            return makeCrossingDataItem(intersectedStart, appendMinutes(intersectedStart, intersectedItem.getDuration().duration()),
                                    itemStart, itemEnd,
                                    therapistCross ? intersectedTherapist : null,
                                    roomCross ? intersectedRoom : null,
                                    clientsCross);
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull);
    }

    private static CrossingDataItem makeCrossingDataItem(Time intersectedStart, Time intersectedEnd,
                                                         Time intersectingStart, Time intersectingEnd,
                                                         Therapist therapist, Room room, List<Client> clients) {

        Time startCrossing = isAfter(intersectingStart, intersectedStart) ? intersectingStart : intersectedStart;
        Time endCrossing = isBefore(intersectingEnd, intersectedEnd) ? intersectingEnd : intersectedEnd;

        int crossingDuration = (int) ((endCrossing.getTime() - startCrossing.getTime()) / (60 * 1000));

        return new CrossingDataItem(startCrossing, crossingDuration, therapist, room, clients, null);
    }

    private static Time appendMinutes(Time time, int durationMinutes) {
        return new Time(time.getTime() + (durationMinutes * 60 * 1000));
    }

    private static boolean isAfter(Time a, Time b) {
        return a.getTime() > b.getTime();
    }

    private static boolean isBefore(Time a, Time b) {
        return a.getTime() < b.getTime();
    }

}
