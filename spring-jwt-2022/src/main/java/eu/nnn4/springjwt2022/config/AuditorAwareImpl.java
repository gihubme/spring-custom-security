package eu.nnn4.springjwt2022.config;

import eu.nnn4.springjwt2022.model.User;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        String name=null;
        if(SecurityContextHolder.getContext().getAuthentication()!=null &&
                SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof User)
            name=((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getName();
        if(name==null)
            return Optional.empty();
        return Optional.of(name);
    }

}