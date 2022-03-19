package eu.nnn4.springjwt2022.security;

import java.util.Collection;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.Transient;

public class CustomAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private String organization;

    public CustomAuthenticationToken(Object principal, Object credentials, String organization) {
        super(principal, credentials);
        this.organization = organization;
        super.setAuthenticated(false);
    }

    public CustomAuthenticationToken(Object principal, Object credentials, String organization,
                                     Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
        this.organization = organization;
        super.setAuthenticated(true); // must use super, as we override
    }

    public String getOrganization() {
        return this.organization;
    }
}