package ca.freshstart.applications.auth;

import ca.freshstart.types.AbstractController;
import ca.freshstart.data.appUser.entity.AppUser;
import ca.freshstart.exceptions.BadRequestException;
import ca.freshstart.exceptions.NotFoundException;
import ca.freshstart.applications.auth.types.PasswordRequest;
import ca.freshstart.types.RestoreData;
import ca.freshstart.applications.auth.types.RestoreRequest;
import ca.freshstart.helpers.ExpiredHashMap;
import ca.freshstart.helpers.ModelValidation;
import ca.freshstart.helpers.PasswordEncryptor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
public class PasswordController extends AbstractController implements InitializingBean {
    @Value("${time.passwordKey.unit}")
    private TimeUnit timePasswordKeyUnit;

    @Value("${time.passwordKey.value}")
    private long timePasswordKeyValue;

    private Map<String, Long> securityKeyMap;

    @Override
    public void afterPropertiesSet() throws Exception {
        securityKeyMap = new ExpiredHashMap<>(timePasswordKeyUnit, timePasswordKeyValue);
    }

    /**
     * Change password
     * @param passwordRequest new password value. plane text
     */
    @RequestMapping(value = "/password", method = {RequestMethod.PUT})
    @ResponseStatus(value= HttpStatus.OK, reason = "Password was changed")
    public void changePassword(@RequestBody PasswordRequest passwordRequest) throws Exception {
        AppUser user = getCurrentUser();

        if(!ModelValidation.isPasswordValid(passwordRequest.getPassword())) {
            throw new BadRequestException("Password doesn't match rules");
        }

        user.setPassword(PasswordEncryptor.encryptPassword(passwordRequest.getPassword()));
        userRepository.save(user);
    }

    /**
     * Request restore password key
     * @param restoreRequest email of user to restore password
     */
    @RequestMapping(value = "/restore", method = {RequestMethod.POST})
    @ResponseStatus(value= HttpStatus.OK, reason = "Request is send")
    public void restorePassword(@RequestBody RestoreRequest restoreRequest) throws Exception {
        Optional<AppUser> userOptional = userRepository.findByEmail(restoreRequest.getEmail());

        if(!userOptional.isPresent()) {
            // mb throw exception ?
            return;
        }

        AppUser user = userOptional.get();

        String key = UUID.randomUUID().toString();

        securityKeyMap.put(key, user.getId());

        mailService.sendBasic(user.getEmail(),"Key to restore password: " + key);
    }

    /**
     * Perform password restore
     * @param restoreData email, key and new password
     */
    @RequestMapping(value = "/restore", method = {RequestMethod.PUT})
    @ResponseStatus(value= HttpStatus.OK, reason = "Password was changed")
    public void restorePassword1(@RequestBody RestoreData restoreData) throws Exception {
        if(!ModelValidation.isPasswordValid(restoreData.getPassword())) {
            throw new BadRequestException("Password doesn't match rules");
        }

        Long id = securityKeyMap.get(restoreData.getSecurityKey());

        if(id == null) {
            throw new BadRequestException("Secret key expired");
        }

        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No such user"));

        user.setPassword(PasswordEncryptor.encryptPassword(restoreData.getPassword()));

        userRepository.save(user);

        mailService.sendRecoveryPassword(user.getEmail(),restoreData.getPassword());
    }
}