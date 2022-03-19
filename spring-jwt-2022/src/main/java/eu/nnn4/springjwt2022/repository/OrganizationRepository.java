package eu.nnn4.springjwt2022.repository;

import eu.nnn4.springjwt2022.model.ERole;
import eu.nnn4.springjwt2022.model.Organization;
import eu.nnn4.springjwt2022.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findByName(String name);

    Optional<Organization> findByCode(String code);
}
