package ca.freshstart.helpers;

import ca.freshstart.types.Constants;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Calendar;
import java.util.Arrays;
import java.util.stream.IntStream;

import static java.time.temporal.ChronoUnit.DAYS;

public final class DateUtils {

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.DATE_FORMAT);
    private static SimpleDateFormat simpleTimeFormat = new SimpleDateFormat(Constants.TIME_FORMAT);
    private static SimpleDateFormat remoteDateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_REMOTE);
    private static SimpleDateFormat remoteUrlDateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_URL_REMOTE);
    private static SimpleDateFormat remoteTimeFormat = new SimpleDateFormat(Constants.TIME_FORMAT_REMOTE);

    public static boolean isDatesEquals(Date dateOne, Date dateTwo) {
        // todo check for nulls

        return toLocalDate(dateOne).equals(toLocalDate(dateTwo));
    }

    public static int compareDateDays(Date date1, Date date2) {
        return (int) date1.getTime() / 1000 - (int) date2.getTime() / 1000;
    }

    public static Date getNextDay(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, 1);
        return new Date(c.getTimeInMillis());
    }

    public static boolean dateInDayRange(Date inputDate, int[] dayRange){
        if (inputDate != null && dayRange != null){
            return IntStream.of(dayRange).anyMatch(x -> x == inputDate.getDay());
        }
        return false;
    }

    public static Date fromLocalDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDate toLocalDate(Date date) {

        if (date instanceof java.sql.Date) {
            return ((java.sql.Date) date).toLocalDate();
        }

        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static boolean isLessDaysBetween(Date dateFrom, Date dateTo, long daysBetweenCheck) {
        long daysBetween = DAYS.between(dateFrom.toInstant(), dateTo.toInstant());
        return daysBetween <= daysBetweenCheck;
    }

    public static boolean isDatesFromToValid(Date dateFrom, Date dateTo) {
        long daysBetween = DAYS.between(dateFrom.toInstant(), dateTo.toInstant());
        return daysBetween >= 0;
    }

    public static Date toDate(String date) {
        try {
            return simpleDateFormat.parse(date);
        } catch (ParseException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static String toDateFormat(Date date) {
        return simpleDateFormat.format(date);
    }

    public static String toTimeFormat(Date time) {
        return simpleTimeFormat.format(time);
    }

    public static String toRemoteDateFormat(Date date) {
        return remoteDateFormat.format(date);
    }

    public static String toRemoteRulDateFormat(Date date) {
        return remoteUrlDateFormat.format(date);
    }

    public static String toRemoteTimeFormat(Date time) {
        return remoteTimeFormat.format(time);
    }

    public static Time getTime(Date date) {
        long timeMs = date.getTime() % Constants.MILLISECONDS_PER_DAY;
        return new Time(timeMs);
    }

    public static Date getDayStart(Date date) {
        long timeMs = date.getTime() % Constants.MILLISECONDS_PER_DAY;
        long dayMs = date.getTime() - timeMs;
        return new Date(dayMs);
    }

}
