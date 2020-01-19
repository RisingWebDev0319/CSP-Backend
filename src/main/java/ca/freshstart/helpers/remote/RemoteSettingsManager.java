package ca.freshstart.helpers.remote;

import ca.freshstart.data.client.entity.Client;
import ca.freshstart.data.client.repository.ClientRepository;
import ca.freshstart.data.concreteEvent.entity.ConcreteEvent;
import ca.freshstart.data.event.entity.Event;
import ca.freshstart.data.event.repository.EventRepository;
import ca.freshstart.data.healthSection.entity.HsColumn;
import ca.freshstart.data.healthSection.entity.HsSelectColumn;
import ca.freshstart.data.healthSection.entity.HsSelectColumnValue;
import ca.freshstart.data.healthSection.repository.HsColumnRepository;
import ca.freshstart.data.healthSection.repository.HsSelectColumnRepository;
import ca.freshstart.data.healthSection.types.HsTableType;
import ca.freshstart.data.protocol.entity.Protocol;
import ca.freshstart.data.protocol.repository.ProtocolRepository;
import ca.freshstart.data.reconcile.entity.ConcreteEventReconcile;
import ca.freshstart.data.reconcile.entity.Estimate;
import ca.freshstart.data.remoteServerSettings.entity.RemoteServerSettings;
import ca.freshstart.data.remoteServerSettings.repository.RemoteServerSettingRepository;
import ca.freshstart.data.service.entity.Service;
import ca.freshstart.data.service.repository.ServiceRepository;
import ca.freshstart.data.therapist.entity.Therapist;
import ca.freshstart.data.therapist.repository.TherapistRepository;
import ca.freshstart.exceptions.NotFoundException;
import ca.freshstart.helpers.CspUtils;
import ca.freshstart.helpers.DateUtils;
import ca.freshstart.helpers.remote.types.*;
import ca.freshstart.types.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Component
public class RemoteSettingsManager {


    @Value("${netSuite.apiKey:}")
    private String nsApiKey;

    @Value("${netSuite.apiEndpoint:}")
    private String nsApiEndpoint;

    @Autowired
    protected TherapistRepository therapistRepository;
    @Autowired
    protected ServiceRepository serviceRepository;
    @Autowired
    protected EventRepository eventRepository;
    @Autowired
    protected ClientRepository clientRepository;
    @Autowired
    protected RemoteServerSettingRepository remoteServerRepository;
    @Autowired
    protected ProtocolRepository protocolRepository;
    @Autowired
    protected RemoteDataService remoteDataService;
    @Autowired
    protected HsColumnRepository hsColumnRepository;
    @Autowired
    protected HsSelectColumnRepository hsSelectColumnRepository;


    public void initializeRemoteUrls() {
        RemoteServerSettings settings;
        if (remoteServerRepository.countAll() > 0) {
            List<RemoteServerSettings> all = remoteServerRepository.findAll();
            if (all == null || all.isEmpty()) {
                throw new NotFoundException("NO SETTINGS");
            }
            settings = all.get(0);
        } else {
            settings = new RemoteServerSettings();

            RemoteServerSettings.RemoteUrls urls = new RemoteServerSettings.RemoteUrls();

            urls.setTherapists(formRemoteSettingsUrl("method=getTherapists"));
            urls.setEvents(formRemoteSettingsUrl("method=getEvents"));
            urls.setServices(formRemoteSettingsUrl("method=getServices"));
            urls.setClients(formRemoteSettingsUrl("method=getClients",
                    "pageSize=" + RemoteDataService.PAGE_SIZE_STR,
                    "page=" + RemoteDataService.PAGE_STR));
            urls.setClientInfo(formRemoteSettingsUrl("method=getClientInformation",
                    "id=" + RemoteDataService.ID_STR));
            urls.setProtocols(formRemoteSettingsUrl("method=getProtocols", "isProtocol=T", "isPackage=F"));
            urls.setProtocolPackages(formRemoteSettingsUrl("method=getProtocols", "isProtocol=F", "isPackage=T"));
            urls.setEstimates(formRemoteSettingsUrl("method=createEstimate"));

            settings.setUrls(urls);

            settings.setCronExpression("0 0 1 * * ?");
            settings.setCronTimezone("GMT+3");

            remoteServerRepository.save(settings);
        }

        remoteDataService.setRemoteUrls(settings.getUrls());
    }

    private String formRemoteSettingsUrl(String... args) {
        StringBuilder builder = new StringBuilder(nsApiEndpoint);
        builder.append("&API_KEY=").append(nsApiKey);
        for (String arg : args) {
            builder.append("&").append(arg);
        }
        return builder.toString();
    }


    public void updateDataFromRemote() {
        updateTherapists();
        updateServices();
        updateEvents();
        updateProtocols();
        updateProtocolPackages();
        updateClients();
    }

    public EstimateIdResponse saveEstimateToRemote(Estimate estimate) {
        if (estimate.getExternalId() != null) {
            return null;
        }

        Client client = clientRepository.findOne(estimate.getClientId());
        if (client == null) {
            return null;
        }
        Long clientExternalId = client.getExternalId();

        List<EstimateItemRemote> items = estimate.getEvents()
                .stream()
                .map((ConcreteEventReconcile event) -> {
                    ConcreteEvent concreteEvent = event.getConcreteEvent();
                    Service service = concreteEvent.getService();

                    if (service == null) {
                        return null;
                    }

                    EstimateItemRemote estimateItemRemote = new EstimateItemRemote();
                    estimateItemRemote.setServiceID(service.getExternalId());
                    estimateItemRemote.setTherapistID(concreteEvent.getTherapist().getExternalId());
                    estimateItemRemote.setPrice(service.getPrice());
                    estimateItemRemote.setServiceDate(DateUtils.toRemoteDateFormat(concreteEvent.getDate()));
                    estimateItemRemote.setServiceTime(DateUtils.toRemoteTimeFormat(concreteEvent.getTime()));
                    return estimateItemRemote;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());


        String trandate = DateUtils.toRemoteRulDateFormat(new Date());
        long date = (new Date()).getTime() + (30 * 24 * 60 * 60 * 1000); // now + one month
        String expectedclosedate = DateUtils.toRemoteRulDateFormat(new Date(date));

        return remoteDataService.sendEstimate(clientExternalId, trandate, expectedclosedate, items);
    }

    private <TR extends IdIf<IDR>, IDR extends Serializable, T extends /*IdIf<ID> &*/ ExternalIdIf<IDR> & Updateable<TR>, ID extends Serializable>
    List<T> updateItems(Stream<TR> remoteStream,
                        ExternalIdRepositoryIf<T, IDR> repository,
                        Function<TR, T> constructor) {

        Map<IDR, TR> remoteMap = remoteStream.collect(Collectors.toMap(TR::getId, itemRemote -> itemRemote));

        List<IDR> ids = remoteMap
                .values().stream()
                .map(TR::getId)
                .collect(Collectors.toList());

        List<T> items;

        if (!ids.isEmpty()) {
            items = repository.findByExternalIds(ids);
            items.forEach(item -> {
                IDR id = item.getExternalId();
                TR remoteItem = remoteMap.get(id);

                if (remoteItem != null) {
                    item.update(remoteItem);
                    remoteMap.remove(id);
                }
            });
        } else {
            items = new ArrayList<>();
        }

        List<T> newItems = remoteMap.values()
                .stream()
                .map(constructor)
                .collect(Collectors.toList());
        items.addAll(newItems);

        repository.save(items);
        return items;
    }

    private void updateTherapists() {
        Function<TherapistRemote, Therapist> constructor = Therapist::new;
        updateItems(remoteDataService.getTherapists(), therapistRepository, constructor);
    }


    private void updateServices() {
        Function<ServiceRemote, Service> trtFunction = Service::new;
        updateItems(remoteDataService.getServices(), serviceRepository, trtFunction);
    }

    private void updateEvents() {
        Function<EventRemote, Event> constructor = Event::new;
        updateItems(remoteDataService.getEvents(), eventRepository, constructor);
    }

    private void updateProtocols() {
        Function<ProtocolRemote, Protocol> constructor = Protocol::new;
        updateItems(remoteDataService.getProtocols(), protocolRepository, constructor);

        changeSelectProtocolsColumn(HsTableType.protocols, "Protocols", protocolRepository.findProtocols());
    }

    private void updateProtocolPackages() {
        Function<ProtocolRemote, Protocol> constructor = Protocol::new;
        updateItems(remoteDataService.getProtocolPackages(), protocolRepository, constructor);

        changeSelectProtocolsColumn(HsTableType.packages, "Protocols Package", protocolRepository.findProtocolsPackages());
    }

    private void changeSelectProtocolsColumn(HsTableType sectionType, String columnTitle, List<Protocol> protocols) {
        HsColumn column = hsColumnRepository.findBySectionTypeAndTitle(sectionType, columnTitle)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No '" + columnTitle + "' column found"));

        HsSelectColumn selectColumn = column.getSelectColumn();

        if (selectColumn == null) {
            selectColumn = new HsSelectColumn();
            selectColumn.setSectionType(sectionType);
            selectColumn = hsSelectColumnRepository.save(selectColumn);
            column.setSelectColumn(selectColumn);
        }

        List<HsSelectColumnValue> values = new ArrayList<>();
        int pos = 0;
        for (Protocol protocol : protocols) {
            HsSelectColumnValue value = new HsSelectColumnValue();
            value.setTitle(protocol.getName());
            value.setValue(protocol.getExternalId());
            value.setPosition(pos++);
            value.setPermanent(true);
            values.add(value);
        }

        CspUtils.mergeValues(
                selectColumn.getValues(),
                values,
                selectColumn::removeValue,
                selectColumn::addValue,
                (to, from) -> {
                    to.setTitle(from.getTitle());
                    to.setValue(from.getValue());
                    to.setPosition(from.getPosition());
                });

        hsColumnRepository.save(column);
    }

    private void updateClients() {

        Function<ClientRemote, Client> constructor = Client::new;
        List<ClientRemote> remoteClients;
        int pageSize = 10;
        int pageId = 0;
        do {
            remoteClients = remoteDataService.getClients(pageSize, pageId++);

            List<Client> clients = updateItems(remoteClients.stream(), clientRepository, constructor);

            clients.forEach(client -> {
                ClientInformationRemote clientInfo = remoteDataService.getClientInfo(client.getExternalId());
                client.updateFromRemoteInfo(clientInfo);
            });

            clientRepository.save(clients);
        } while (remoteClients.size() == pageSize);

    }

}
