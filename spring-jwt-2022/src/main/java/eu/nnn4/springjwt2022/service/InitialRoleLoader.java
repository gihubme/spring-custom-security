package eu.nnn4.springjwt2022.service;

import eu.nnn4.springjwt2022.model.ERole;
import eu.nnn4.springjwt2022.model.Role;
import eu.nnn4.springjwt2022.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class InitialRoleLoader {
	
	@Autowired
	private RoleService roleService;

	@PostConstruct
	public void roleInitializer() {
		log.info("InitialRoleLoader constructed");
		List<ERole> roles = Arrays.asList(ERole.values());
	    roles.forEach(i -> roleService.createRoleIfNotFound(i));
	}
}
