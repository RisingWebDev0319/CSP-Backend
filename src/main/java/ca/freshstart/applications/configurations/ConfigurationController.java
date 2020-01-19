package ca.freshstart.applications.configurations;

import ca.freshstart.types.AbstractController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class ConfigurationController extends AbstractController {
    @Value("${server.version:}")
    private String serverVersion;

    /**
     * Application version
     */
    @RequestMapping(value = "/version", method = {RequestMethod.GET})
    public Map<String, String> version() {
        HashMap<String, String> map = new HashMap<>();
        map.put("version", serverVersion);
        return map;
    }
}