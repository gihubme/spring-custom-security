package eu.nnn4.springjwt2022.security;

import eu.nnn4.springjwt2022.exception.ResourceNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface CustomUserDetailsService {
    UserDetails loadUserByUsernameAndOrganizationCode(String email, String domain) throws ResourceNotFoundException;
}
