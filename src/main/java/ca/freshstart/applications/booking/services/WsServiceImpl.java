package ca.freshstart.applications.booking.services;


import ca.freshstart.applications.booking.interfaces.WsService;
import ca.freshstart.applications.booking.types.BookingEventNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WsServiceImpl implements WsService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public void notifyBookingEvent(BookingEventNotification notification) {
        messagingTemplate.convertAndSend("/topic/bookingEventNotification", notification);
    }
}
