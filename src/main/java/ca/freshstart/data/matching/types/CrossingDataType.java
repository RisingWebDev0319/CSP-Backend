package ca.freshstart.data.matching.types;

public enum CrossingDataType {
    unavailable, // intersection with therapist unavailable time - AvTherapistDayRecord
    confirmation, // intersection with  PreliminaryEvent in confirmation process
    concrete, // intersection with ConcreteEvent
    calendar //  intersection with ConcreteCalendarEvent
}
