package ca.freshstart.applications.initialization;

import ca.freshstart.data.appUser.entity.AppUser;
import ca.freshstart.data.appUser.repository.UserRepository;
import ca.freshstart.data.concreteEvent.entity.ConcreteEventSubStatus;
import ca.freshstart.data.concreteEvent.repositories.ConcreteEventSubStatusRepository;
import ca.freshstart.helpers.PasswordEncryptor;
import ca.freshstart.helpers.remote.RemoteSettingsManager;
import ca.freshstart.types.Module;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AppDbInitializer {

    private static final Logger logger = LoggerFactory.getLogger(AppDbInitializer.class);

    private final UserRepository userRepository;

    private final RemoteSettingsManager remoteManager;

    private final ConcreteEventSubStatusRepository concreteEventSubStatusRepository;


    public void init() {
        initSuperUser();
        initRemoteSettings();
        initConcreteEventSubStatuses();
    }

    private void initSuperUser() {
        Optional<AppUser> byEmail = userRepository.findByEmail("user1@email.com");
        if (byEmail.isPresent()){
            return;
        }

        AppUser superUser = new AppUser();
        superUser.setId(1L);
        superUser.setName("User 1");
        superUser.setEmail("user1@email.com");
        superUser.setPassword(PasswordEncryptor.encryptPassword("pass1"));
        List<String> modules = Arrays.stream(Module.values())
                .map(module -> module.name())
                .collect(Collectors.toList());
        superUser.setModules(modules);
        superUser.setLocked(false);

        userRepository.save(superUser);
    }


    private void initRemoteSettings() {

        remoteManager.initializeRemoteUrls();

        logger.info("-- Mocked Remote Settings --");
    }

    private void initConcreteEventSubStatuses() {
        if (concreteEventSubStatusRepository.countAll() > 0) {
            return;
        }

        List<ConcreteEventSubStatus> subStatuses =
                Arrays.stream(new String[]{
                        "check in",
                        "check out",
                        "no show",
                        "free by fresh start",
                        "free by therapist"
                }).map(name -> {
                    ConcreteEventSubStatus subStatus = new ConcreteEventSubStatus();
                    subStatus.setName(name);
                    return subStatus;
                }).collect(Collectors.toList());

        concreteEventSubStatusRepository.save(subStatuses);

    }

}
