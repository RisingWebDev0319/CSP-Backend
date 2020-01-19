package ca.freshstart.types;

import ca.freshstart.applications.auth.helpers.CspAuthenticationToken;
import ca.freshstart.applications.auth.helpers.TokenManager;
import ca.freshstart.data.mails.service.MailsService;
import ca.freshstart.data.appUser.entity.AppUser;
import ca.freshstart.data.appUser.repository.UserRepository;
import ca.freshstart.exceptions.UnathorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class AbstractController {
    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected TokenManager tokenManager;

    @Autowired
    protected MailsService mailService;

    protected AppUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if(auth == null) {
            throw new UnathorizedException("Access denied");
        }

        AppUser user = ((CspAuthenticationToken)auth).getUser();

        if(user == null) {
            throw new UnathorizedException("Access denied");
        }

        return user;
    }
}