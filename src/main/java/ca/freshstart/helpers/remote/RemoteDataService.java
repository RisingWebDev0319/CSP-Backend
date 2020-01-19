package ca.freshstart.helpers.remote;


import ca.freshstart.data.remoteServerSettings.entity.RemoteServerSettings;
import ca.freshstart.helpers.CspUtils;
import ca.freshstart.helpers.remote.types.*;
import ca.freshstart.types.EstimateIdResponse;
import com.google.gson.Gson;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class RemoteDataService {

    public static String PAGE_SIZE_STR = "{pageSize}";
    public static String PAGE_SIZE_PATTERN = "\\{pageSize\\}";

    public static String PAGE_STR = "{page}";
    public static String PAGE_PATTERN = "\\{page\\}";

    public static String ID_STR = "{id}";
    public static String ID_PATTERN = "\\{id\\}";

    private final Gson gson = new Gson();

    private RemoteServerSettings.RemoteUrls remoteUrls;

    public void setRemoteUrls(RemoteServerSettings.RemoteUrls remoteUrls) {
        this.remoteUrls = remoteUrls;
    }


    public List<ClientRemote> getClients(int pageSize, int page) {

        String url = remoteUrls.getClients()
                .replaceFirst(PAGE_SIZE_PATTERN, "" + pageSize)
                .replaceFirst(PAGE_PATTERN, "" + page);

        String json = CspUtils.urlToString(url);

        ClientRemote[] array = gson.fromJson(json, ClientRemote[].class);

        return Arrays.stream(array).collect(Collectors.toList());// return ArrayList implementation
    }

    public ClientInformationRemote getClientInfo(long clientId) {
        String url = remoteUrls.getClientInfo()
                .replaceFirst(ID_PATTERN, "" + clientId);

        String json = CspUtils.urlToString(url);

        return gson.fromJson(json, ClientInformationRemote.class);
    }

    public Stream<TherapistRemote> getTherapists() {
        return getItemsList(remoteUrls.getTherapists(), TherapistRemote[].class);
    }

    public Stream<ServiceRemote> getServices() {
        return getItemsList(remoteUrls.getServices(), ServiceRemote[].class);
    }

    public Stream<EventRemote> getEvents() {
        return getItemsList(remoteUrls.getEvents(), EventRemote[].class);
    }

    public Stream<ProtocolRemote> getProtocols() {
        return getItemsList(remoteUrls.getProtocols(), ProtocolRemote[].class);
    }

    public Stream<ProtocolRemote> getProtocolPackages() {
        return getItemsList(remoteUrls.getProtocolPackages(), ProtocolRemote[].class);
    }

    private <T> Stream<T> getItemsList(String url, Class<T[]> type) {
        String json = CspUtils.urlToString(url);
        T[] array = gson.fromJson(json, type);
//        return Arrays.stream(array).collect(Collectors.toList());
        return Arrays.stream(array);
    }

    public EstimateIdResponse sendEstimate(Long clientId,
                                           String trandate,
                                           String expectedclosedate,
                                           List<EstimateItemRemote> items) {
        String itemsJson = gson.toJson(items);

        String encodedItemsJson;
        try {
            encodedItemsJson = URLEncoder.encode(itemsJson, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            encodedItemsJson = itemsJson;
        }

        String url = remoteUrls.getEstimates() +
                "&clientId=" + clientId +
                "&trandate=" + trandate +
                "&expectedclosedate=" + expectedclosedate +
                "&items=" + encodedItemsJson;
        String json = CspUtils.urlToString(url);
        return gson.fromJson(json, EstimateIdResponse.class);
    }
}
