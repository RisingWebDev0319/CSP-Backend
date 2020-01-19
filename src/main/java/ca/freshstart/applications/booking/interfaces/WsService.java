package ca.freshstart.applications.booking.interfaces;


import ca.freshstart.applications.booking.types.BookingEventNotification;

public interface WsService {
    void notifyBookingEvent(BookingEventNotification notification);
}
