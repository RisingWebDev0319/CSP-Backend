package ca.freshstart.helpers;

import ca.freshstart.data.appUser.entity.AppUser;
import ca.freshstart.data.calendarEvent.repository.CalendarEventRepository;
import ca.freshstart.data.equipment.entity.Equipment;
import ca.freshstart.data.event.entity.Event;
import ca.freshstart.data.week.entity.Week;
import ca.freshstart.data.eventRecord.entity.EventRecord;
import ca.freshstart.data.restriction.entity.Restriction;
import ca.freshstart.data.room.entity.Room;
import ca.freshstart.data.serviceCategory.entity.ServiceCategory;
import ca.freshstart.data.availability.entity.AvRequest;
import ca.freshstart.data.availability.entity.AvTherapistDayRecord;
import ca.freshstart.data.availability.entity.AvTherapistRequest;
import ca.freshstart.data.calendarEvent.entity.CalendarEvent;
import ca.freshstart.data.concreteCalendarEvent.entity.ConcreteCalendarEvent;
import ca.freshstart.data.session.entity.Session;
import ca.freshstart.data.therapist.entity.Therapist;
import ca.freshstart.data.suggestions.entity.SsColumnDefinition;
import ca.freshstart.data.suggestions.entity.SsCustomColumn;
import ca.freshstart.data.therapist.entity.TherapistInfo;
import ca.freshstart.exceptions.BadRequestException;
import lombok.RequiredArgsConstructor;

import java.util.regex.Pattern;
import java.util.*;

import static ca.freshstart.helpers.CspUtils.isNullOrEmpty;
import static ca.freshstart.helpers.DateUtils.isDatesFromToValid;
import static java.lang.String.format;

public class ModelValidation {
    private static final Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$");

    public static void validateUser(AppUser user) {
        if (user == null) {
            throw new BadRequestException("User object is null");
        }

        if (CspUtils.isNullOrEmpty(user.getName())) {
            throw new BadRequestException(format("Field %s shouldn't be empty", "name"));
        }

        if (!isEmailValid(user.getEmail())) {
            throw new BadRequestException("User email is not valid");
        }

        if (!isPasswordValid(user.getPassword())) {
            throw new BadRequestException("User password is not valid");
        }

        if (user.getName().length() > 255) {
            throw new BadRequestException("Name should be less then 255 characters");
        }
    }

    public static boolean isPasswordValid(String password) {
        return !(password == null || password.length() < 3);
    }

    public static boolean isEmailValid(String email) {
        if (CspUtils.isNullOrEmpty(email)) {
            return false;
        }

        return emailPattern.matcher(email).matches();
    }

    public static void validateServiceCategory(ServiceCategory serviceCategory) {
        if (CspUtils.isNullOrEmpty(serviceCategory.getName())) {
            throw new BadRequestException("Name shouldn't be empty");
        }

        if (serviceCategory.getName().length() > 255) {
            throw new BadRequestException("Name should be less then 255 characters");
        }
    }

    public static void validateWeek(Week week) {
        if (CspUtils.isNullOrEmpty(week.getName())) {
            throw new BadRequestException("Name shouldn't be empty");
        }

        if (week.getName().length() > 255) {
            throw new BadRequestException("Name should be less then 255 characters");
        }
    }


    public static void validateTherapistInfo(TherapistInfo info) {
        if (CspUtils.isNullOrEmpty(info.getFirstName())) {
            throw new BadRequestException("Therapist First name is required");
        }
    }

    public static void validateRoom(Room room) {
        if (CspUtils.isNullOrEmpty(room.getName())) {
            throw new BadRequestException(format("Room %s shouldn't be empty", "name"));
        }

        if (room.getCapacity() == null) {
            throw new BadRequestException(format("Room %s shouldn't be null", "capacity"));
        }

        if (room.getName().length() > 255) {
            throw new BadRequestException("Room name should be less then 255 characters");
        }
    }

    public static void validateRestriction(Restriction restriction) {
        if (CspUtils.isNullOrEmpty(restriction.getName())) {
            throw new BadRequestException(format("Field %s shouldn't be empty", "name"));
        }

        if (restriction.getType() == null) {
            throw new BadRequestException(format("Field %s shouldn't be empty", "type"));
        }

        if (restriction.getLinkedId() == null) {
            throw new BadRequestException(format("Field %s shouldn't be empty", "linkedId"));
        }

        if (restriction.getName().length() > 255) {
            throw new BadRequestException("Name should be less then 255 characters");
        }
    }

    public static void validateSession(Session session) {
        if (CspUtils.isNullOrEmpty(session.getName())) {
            throw new BadRequestException(format("Field %s shouldn't be empty", "name"));
        }

        if (session.getEndDate() == null) {
            throw new BadRequestException(format("Field %s shouldn't be empty", "endDate"));
        }

        if (session.getStartDate() == null) {
            throw new BadRequestException(format("Field %s shouldn't be empty", "startDate"));
        }

        if (isNullOrEmpty(session.getClients())) {
            throw new BadRequestException(format("Field %s shouldn't be empty", "clients"));
        }

        if (isNullOrEmpty(session.getTherapists())) {
            throw new BadRequestException(format("Field %s shouldn't be empty", "therapist"));
        }

        if (session.getName().length() > 255) {
            throw new BadRequestException("Name should be less then 255 characters");
        }
    }

    public static void validateAvailabilityRequest(AvRequest request) {
        if (isNullOrEmpty(request.getTherapistsRequests())) {
            throw new BadRequestException(format("Field %s shouldn't be empty", "therapistsRequests"));
        }

        request.getTherapistsRequests().forEach(tr -> validateAvailabilityTherapistRequest(tr));

        if (request.getEndDate() == null) {
            throw new BadRequestException(format("Field %s shouldn't be empty", "endDate"));
        }

        if (request.getStartDate() == null) {
            throw new BadRequestException(format("Field %s shouldn't be empty", "startDate"));
        }
    }

    public static void validateAvailabilityTherapistRequest(AvTherapistRequest therapistRequest) {
        if (therapistRequest.getTherapistId() == null) {
            throw new BadRequestException(format("Field %s shouldn't be empty", "therapistId"));
        }
    }

    public static void validateAvailTherapistDayRecord(AvTherapistDayRecord dayRecord) {
        if (dayRecord.getDate() == null) {
            throw new BadRequestException(format("Field %s shouldn't be empty", "date"));
        }
    }

    public static void validateEquipment(Equipment equipment) {
        if (CspUtils.isNullOrEmpty(equipment.getName())) {
            throw new BadRequestException(format("Field %s shouldn't be empty", "name"));
        }

        if (equipment.getCapacity() == null) {
            throw new BadRequestException(format("Field %s shouldn't be empty", "capacity"));
        }

        if (equipment.getName().length() > 255) {
            throw new BadRequestException("Name should be less then 255 characters");
        }
    }

    public static void validateCalendarEventRequest(CalendarEvent calendarEvent) {
        String templateEmptyError = "Field %s shouldn't be empty";

        if (CspUtils.isNullOrEmpty(calendarEvent.getName())) {
            throw new BadRequestException(format(templateEmptyError, "name"));
        }

        if (calendarEvent.getDateStart() == null) {
            throw new BadRequestException(format(templateEmptyError, "dateStart"));
        }

        if (calendarEvent.getDateEnd() == null) {
            throw new BadRequestException(format(templateEmptyError, "dateEnd"));
        }

        if (calendarEvent.getCapacity() == null) {
            throw new BadRequestException(format(templateEmptyError, "capacity"));
        }

        if (calendarEvent.getDays() == null) {
            throw new BadRequestException(format(templateEmptyError, "days"));
        }

        Event event = calendarEvent.getEvent();
        if (event == null) {
            throw new BadRequestException(format(templateEmptyError, "event"));
        }

        if (!isDatesFromToValid(calendarEvent.getDateStart(), calendarEvent.getDateEnd())) {
            throw new BadRequestException("DateStart should be before or equal to DateEnd");
        }

        // CHECK: what is th reson for this requirement - with one after it it allws strting oly from today
        /*if(!isDatesFromToValid(calendarEvent.getDateStart(), new Date())) {
            throw new BadRequestException("You can't change startDate in the past");
        }*/

//        if (!isDatesFromToValid(new Date(), calendarEvent.getDateStart())) {
//            throw new BadRequestException("You can't change calendarEvent in the past");
//        }

        Long eventId = event.getId();
        Therapist therapist = calendarEvent.getTherapist();
        if (therapist != null) {
            boolean isTherapistCanDoEvent = therapist.getEvents().stream().anyMatch((EventRecord eventRecord) -> eventRecord.getEvent().getId().equals(eventId));
            if (!isTherapistCanDoEvent) {
                throw new BadRequestException(format("Therapist (%s) can't do the event (%s)", therapist.getName(), event.getName()));
            }
        }

    }

    public static void validateCustomColumn(SsCustomColumn customColumn) {
        if (CspUtils.isNullOrEmpty(customColumn.getTitle())) {
            throw new BadRequestException(format("Column %s shouldn't be empty", "title"));
        }

//        if(isNullOrEmpty(customColumn.getValues())) {
//            throw new BadRequestException(format("Column %s shouldn't be empty", "values"));
//        }

        if (customColumn.getType() == null) {
            throw new BadRequestException(format("Column %s shouldn't be null", "type"));
        }

        if (customColumn.getTitle().length() > 255) {
            throw new BadRequestException("Column title should be less then 255 characters");
        }
    }

    public static void validateSsColumnDefinition(SsColumnDefinition definition) {
        if (CspUtils.isNullOrEmpty(definition.getTitle())) {
            throw new BadRequestException(format("Column %s shouldn't be empty", "title"));
        }

        if (definition.getType() == null) {
            throw new BadRequestException(format("Column %s shouldn't be null", "type"));
        }

        if (definition.getCustomColumnId() == null) {
            throw new BadRequestException(format("Column %s shouldn't be null", "customColumnId"));
        }

        if (definition.getTitle().length() > 255) {
            throw new BadRequestException("Column title should be less then 255 characters");
        }
    }

    public static void validateConcreteCalendarEvent(ConcreteCalendarEvent concreteCalendarEvent, CalendarEvent calendarEvent) {
        if (CspUtils.isNullOrEmpty(concreteCalendarEvent.getName())) {
            throw new BadRequestException(format("Field %s shouldn't be empty", "name"));
        }

        if (concreteCalendarEvent.getDate() == null) {
            throw new BadRequestException(format("Field %s shouldn't be empty", "date"));
        }

        Long minimalCapacity = calendarEvent.getMinimalCapacity();
        int size = concreteCalendarEvent.getClients().size();
        if (minimalCapacity != 0 && size > minimalCapacity) { // 0 - is infinite capacity
            throw new BadRequestException(format("The number of clients (%d) exceeds the capacity of the Calendar Event (%d)", size, minimalCapacity));
        }
    }
}
