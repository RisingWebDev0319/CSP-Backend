package ca.freshstart.applications.auth.helpers;

import ca.freshstart.data.appUser.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class CspAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserRepository userRepository;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        CspAuthenticationToken cspAuthentication = (CspAuthenticationToken) authentication;

        ((CspAuthenticationToken) authentication).getUser();

//        User user = userRepository.findOne(demoAuthentication.getId());

//        if(user == null) {
//            throw new RuntimeException("Could not find user with ID: " + demoAuthentication.getId());
//        }

        return cspAuthentication;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return CspAuthenticationToken.class.isAssignableFrom(authentication);
    }
}