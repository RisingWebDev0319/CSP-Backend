package ca.freshstart.applications.remoteServer;

import ca.freshstart.types.AbstractController;
import ca.freshstart.data.remoteServerSettings.entity.RemoteServerSettings;
import ca.freshstart.exceptions.NotFoundException;
import ca.freshstart.helpers.remote.RemoteDataService;
import ca.freshstart.data.remoteServerSettings.repository.RemoteServerSettingRepository;
import ca.freshstart.helpers.SchedulerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static ca.freshstart.helpers.CspUtils.isNullOrEmpty;

@RestController
public class RemoteServerController extends AbstractController {
    @Autowired
    private SchedulerManager schedulerManager;
    @Autowired
    private RemoteServerSettingRepository remoteServerRepository;
    @Autowired
    private RemoteDataService remoteDataService;


    /**
     * Get current server settings
     * @return current settings
     */
    @RequestMapping(value = "/remoteServer/settings", method = {RequestMethod.GET})
    @Secured("ROLE_SETTINGS")
    public RemoteServerSettings remoteServerSettingsGet() throws Exception {
        List<RemoteServerSettings> all = remoteServerRepository.findAll();
        if (all == null || all.isEmpty()) {
            throw new NotFoundException("NO SETTINGS");
        }
        return all.get(0);
    }

    /**
     * Set current server settings
     * @param settings settings to set
     */
    @RequestMapping(value = "/remoteServer/settings", method = {RequestMethod.POST})
    @ResponseStatus(value= HttpStatus.OK, reason = "Settings updated")
    @Secured("ROLE_SETTINGS")
    public void remoteServerSettingsPost(@RequestBody RemoteServerSettings settings) throws Exception {
        List<RemoteServerSettings> all = remoteServerRepository.findAll();
        if (all == null || all.isEmpty()) {
            throw new NotFoundException("NO SETTINGS");
        }
        RemoteServerSettings serverSettings = all.get(0);

        serverSettings.setUrls(settings.getUrls());
        serverSettings.setUpdateTime(settings.getUpdateTime());

        boolean cronChanged = !serverSettings.getCronExpression().equals(settings.getCronExpression()) ||
                              !serverSettings.getCronTimezone().equals(settings.getCronTimezone());

        if(!isNullOrEmpty(settings.getCronExpression())) {
            serverSettings.setCronExpression(settings.getCronExpression());
        }

        if(!isNullOrEmpty(settings.getCronTimezone())) {
            serverSettings.setCronTimezone(settings.getCronTimezone());
        }

        remoteServerRepository.save(serverSettings);
        remoteDataService.setRemoteUrls(serverSettings.getUrls());

        if(cronChanged) {
            schedulerManager.rescheduleUpdate(serverSettings);
        }
    }

    /**
     * Force update settings
     */
    @RequestMapping(value = "/remoteServer/update", method = {RequestMethod.POST})
    @ResponseStatus(value= HttpStatus.OK, reason = "Update finished")
    @Secured("ROLE_SETTINGS")
    public void remoteServerSettingsUpdate() throws Exception {
        schedulerManager.updateDataFromRemote();
    }
}