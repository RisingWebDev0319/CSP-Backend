package ca.freshstart.applications.auth;

import ca.freshstart.types.AbstractController;
import ca.freshstart.data.appUser.entity.AppUser;
import ca.freshstart.exceptions.UnathorizedException;
import ca.freshstart.applications.auth.helpers.CspAuthenticationToken;
import ca.freshstart.applications.auth.types.LoginRequest;
import ca.freshstart.applications.auth.types.LoginResponse;
import ca.freshstart.types.Module;
import ca.freshstart.types.Constants;
import ca.freshstart.helpers.PasswordEncryptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.stream.Collectors;

@RestController
public class AuthController extends AbstractController {

    /**
     * Makes login request
     * @param credentials User's credentials
     * @return token
     */
    @RequestMapping(value = "/login", method = {RequestMethod.POST})
    public LoginResponse login(@RequestBody LoginRequest credentials) throws Exception {
        if(!credentials.isValid()) {
            throw new UnathorizedException("Bad credentials. Access denied.");
        }

        String password = PasswordEncryptor.encryptPassword(credentials.getPassword()).trim();

        AppUser user = userRepository.find(credentials.getUsername(), password)
                    .orElseThrow(() -> new UnathorizedException("Bad credentials. Access denied."));

        if(user.getId() == Constants.SUPER_USER_ID) {
            // add all modules to the SUPER_USER
            if(user.getModules().size() != Module.values().length) {
                user.setModules(Arrays.stream(Module.values()).map(Module::name).collect(Collectors.toList()));
                userRepository.save(user);
            }
        }

        Authentication auth = new CspAuthenticationToken(user);

        SecurityContextHolder.getContext().setAuthentication(auth);

        LoginResponse loginResponse = new LoginResponse();

        loginResponse.setToken(tokenManager.generateToken(auth));

        return loginResponse;
    }

    /**
     * Logout from server (make token invalid)
     * @param request just request
     */
    @RequestMapping(value = "/logout", method = {RequestMethod.POST})
    @ResponseStatus(value= HttpStatus.OK, reason = "Logout done")
    public void logout(HttpServletRequest request) throws Exception {
        String xAuth = request.getHeader(Constants.AUTH_HEADER_NAME);

        Authentication auth = tokenManager.getUserByToken(xAuth);

        if(auth == null) {
            throw new UnathorizedException("Bad token in header.");
        }

        tokenManager.removeToken(xAuth);
    }
}