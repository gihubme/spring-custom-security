package eu.nnn4.springjwt2022;

import eu.nnn4.springjwt2022.model.ERole;
import eu.nnn4.springjwt2022.model.Organization;
import eu.nnn4.springjwt2022.model.Role;
import eu.nnn4.springjwt2022.model.User;
import eu.nnn4.springjwt2022.repository.OrganizationRepository;
import eu.nnn4.springjwt2022.repository.UserRepository;
import eu.nnn4.springjwt2022.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@DependsOn(value = "initialRoleLoader")
@ConditionalOnProperty(value = "app.setupdata", havingValue = "true")
@Component
public class InitUserData {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private RoleService roleService;

    @Bean
    public ApplicationRunner userInitializer() {
        log.info("UserInitializer started, roles: "+roleService.getRolesSize());
        return args -> createUsersIfNotFound();
    }

    private void createUsersIfNotFound() {
        if(organizationRepository.findAll().size()==0){
            initOrganizations();
        }
        if(userRepository.findAll().size()==0 ){
            initUsers();
        }

    }

    private void initUsers() {
        List<User> users=new ArrayList<>();

        User user1=initUser("alice",ERole.ROLE_USER,"123123","FirstOrg");
        User user1_c=initUser("com1",ERole.ROLE_COMPANYUSER,"123123","FirstOrg");
        users.add(user1);
        users.add(user1_c);

        User user2=initUser("tom",ERole.ROLE_USER,"123123","SecondOrg");
        User user2_c=initUser("com2",ERole.ROLE_COMPANYUSER,"123123","SecondOrg");
        users.add(user2);
        users.add(user2_c);

        users.stream().forEach(u->log.info(u.getName()+"\n"+u.toString()));

    }

    public User initUser(String name,ERole erole, String pass, String org) {
        User user = new User();
        user.setName(name);
        user.setEmail(name+"@yahoo.com");
        user.setActive(true);

        Role role =  roleService.createRoleIfNotFound(erole);
        user.addRole(role);
        user.setPassword(encoder.encode(pass));
        if(organizationRepository.findByName(org).isPresent()){
            user.setOrganization(organizationRepository.findByName(org).get());
        }
        return userRepository.save(user);
    }

//    @Transactional
    private void initOrganizations() {

        final Organization org1 = new Organization(null,null,"FirstOrg","firstOrg".toLowerCase(),true,true);
        organizationRepository.save(org1);

        final Organization org2 = new Organization(null, null,"SecondOrg","secondorg",true,false);
        organizationRepository.save(org2);
    }

}
