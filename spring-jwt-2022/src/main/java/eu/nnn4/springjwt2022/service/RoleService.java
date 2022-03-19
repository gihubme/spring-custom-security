package eu.nnn4.springjwt2022.service;

import eu.nnn4.springjwt2022.model.ERole;
import eu.nnn4.springjwt2022.model.Role;
import eu.nnn4.springjwt2022.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    public Role createRoleIfNotFound(ERole roleName) {
        Optional<Role> role = roleRepository.findByName(roleName);
        if (!role.isPresent()) {
            Role newRole = new Role();
            newRole.setName(roleName);
            newRole = roleRepository.save(newRole);
            return newRole;
        }
        return role.get();
    }

    public int getRolesSize(){
        return roleRepository.findAll().size();
    }
}
