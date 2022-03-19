package eu.nnn4.springjwt2022.service;

import eu.nnn4.springjwt2022.exception.ResourceNotFoundException;
import eu.nnn4.springjwt2022.model.User;
import eu.nnn4.springjwt2022.repository.UserRepository;
import eu.nnn4.springjwt2022.security.CustomUserDetailsService;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@NoArgsConstructor
@Service(value = "userDetailsService")
public class CustomUserDetailsServiceImpl implements CustomUserDetailsService {
    @Autowired
    private UserRepository userRepository;

    public CustomUserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

//    @Override
//    @Transactional
//    public UserDetails loadUserByUsername(String userIdStr) throws UsernameNotFoundException {
//        try{
//            UserDetails userDetails=getUser(Long.parseLong(userIdStr));
//            return userDetails;
//        } catch (ResourceNotFoundException ex){
//            throw new UsernameNotFoundException("User Not Found with -> id : " + userIdStr);
//        }
//    }

    @Transactional
    public UserDetails loadUserById(Long userId)
        throws ResourceNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", "id", userId)
                );
        return UserPrincipal.build(user);
    }

    @Transactional
    public UserDetails loadUserByUsernameAndOrganizationCode(String email, String organizationCode) throws ResourceNotFoundException {
        User user = userRepository.findByEmailAndOrganization_Code(email,organizationCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", "email and organizationCode", email+" and "+organizationCode)
                );
        return UserPrincipal.build(user);
    }

}
