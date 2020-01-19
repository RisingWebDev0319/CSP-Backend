package ca.freshstart.data.concreteEvent.interfaces;

import ca.freshstart.data.client.entity.Client;
import ca.freshstart.data.service.entity.Service;
import ca.freshstart.data.session.entity.Session;

public interface BaseConcreteEvent extends BaseCrossEvent {
    Service getService();
}
