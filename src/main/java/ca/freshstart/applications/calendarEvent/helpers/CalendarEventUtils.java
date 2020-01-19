package ca.freshstart.applications.calendarEvent.helpers;

import ca.freshstart.data.calendarEvent.entity.CalendarEvent;
import ca.freshstart.data.concreteCalendarEvent.entity.ConcreteCalendarEvent;
import ca.freshstart.helpers.DateUtils;

import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static ca.freshstart.helpers.DateUtils.toLocalDate;
import static java.time.temporal.TemporalAdjusters.nextOrSame;

public final class CalendarEventUtils {

    public static List<ConcreteCalendarEvent> createConcreteCalendarEvents(CalendarEvent calendarEvent, Date start, Date end) {

        LocalDate startDate = toLocalDate(start);
        LocalDate endDate = toLocalDate(end);

        int[] days1 = calendarEvent.getDays();// f.e.  [0, 2] - [SUNDAY, TUESDAY]
        int[] days = Arrays.stream(days1).toArray();// f.e.  [0, 2] - [SUNDAY, TUESDAY]
        for (int i = 0; i < days.length; ++i) {
            if (days[i] == 0) {
                days[i] = 7;
                break;
            }
        }
        Arrays.sort(days);


        List<ConcreteCalendarEvent> concretes = new ArrayList<>();
        Arrays.stream(days).forEach(weekDay -> {
            for (LocalDate theDate = startDate.with(nextOrSame(DayOfWeek.of(weekDay)));
                 theDate.isBefore(endDate) || theDate.isEqual(endDate);
                 theDate = theDate.plus(7, ChronoUnit.DAYS)) {

                ConcreteCalendarEvent eventConcrete = new ConcreteCalendarEvent(calendarEvent);
                eventConcrete.setDate(DateUtils.fromLocalDate(theDate));
                concretes.add(eventConcrete);
            }
        });
        return concretes;
    }

}
