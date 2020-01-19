package ca.freshstart.applications.matching.helpers;


import ca.freshstart.types.EntityId;
import ca.freshstart.data.event.entity.Event;
import ca.freshstart.data.service.entity.Service;
import ca.freshstart.data.concreteEvent.entity.ConcreteEvent;
import ca.freshstart.data.reconcile.entity.ConcreteEventReconcile;
import ca.freshstart.data.suggestions.entity.PreliminaryEvent;
import ca.freshstart.data.concreteEvent.types.ConcreteEventState;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConcreteEventUtils {

    public static List<ConcreteEvent> generateConcreteEventsFromConfirmation(List<PreliminaryEvent> items, List<Service> services) {
        return items.stream().map(item -> {
            Service service = item.getService();

            ConcreteEvent concreteEvent = new ConcreteEvent();
            concreteEvent.setEvent(null);
            concreteEvent.setService(service);
            concreteEvent.setRoom(item.getRoom());
            concreteEvent.setTherapist(item.getTherapist());
            concreteEvent.setClient(item.getClient());
            concreteEvent.setSession(item.getSession());
            concreteEvent.setNote(item.getNote());
            concreteEvent.setTime(item.getTime());
            concreteEvent.setDate(item.getDate());
            concreteEvent.setState(ConcreteEventState.tentative);
            concreteEvent.setSubStatus(null);
            services.stream().filter(serviceItem -> serviceItem.getId() == service.getId()).findFirst().ifPresent(serviceItem -> {
                concreteEvent.setDuration(serviceItem.getTime());
            });
            return concreteEvent;
        }).collect(Collectors.toList());
    }

    public static List<ConcreteEventReconcile> generateConcreteEventReconcile(List<ConcreteEvent> concreteEvents,
                                                                              List<Service> services) {

        Map<Long, Service> id2Service = services.stream().collect(Collectors.toMap(EntityId::getId, o -> o));

        return concreteEvents.stream().map(concreteEvent -> {
            Float price = getCostOfConcreteEvent(concreteEvent, id2Service);
            return new ConcreteEventReconcile(concreteEvent, price);
        }).collect(Collectors.toList());
    }

    public static Float getCostOfConcreteEvent(ConcreteEvent concreteEvent, Map<Long, Service> id2Service){
        Long serviceId = concreteEvent.getService().getId();
        Service service = id2Service.get(serviceId);
        Event event = concreteEvent.getEvent();
        return  (serviceId != null && service != null) ?
                service.getPrice() : event.getPrice();
    }
}
