package ca.freshstart.applications.auth.helpers;

import ca.freshstart.data.appUser.entity.AppUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class CspAuthenticationToken extends UsernamePasswordAuthenticationToken {
    private AppUser user;

    public CspAuthenticationToken(Object principal, Object credentials) {
        super(principal, credentials);
    }

    public CspAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }

    public CspAuthenticationToken(AppUser user) {
        super(user.getEmail(), null, getUserAuths(user));

        this.user = user;
    }

    private static Collection<? extends GrantedAuthority> getUserAuths(AppUser user) {
        if(user.getModules() == null)
            return Collections.emptyList();
        return user.getModules().stream().map(m -> new SimpleGrantedAuthority("ROLE_" + m)).collect(Collectors.toList());
    }

    public AppUser getUser() {
        return user;
    }
}