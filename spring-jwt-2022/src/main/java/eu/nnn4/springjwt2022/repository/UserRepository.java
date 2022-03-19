package eu.nnn4.springjwt2022.repository;

import eu.nnn4.springjwt2022.model.ERole;
import eu.nnn4.springjwt2022.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("select u from User u "+
            "left join fetch u.organization o "+
            "left join fetch u.roles "+
            "where u.email= :email and o.code= :code")
    Optional<User> findByEmailAndOrganization_Code(@Param("email")String email,@Param("code")String organizationCode);

    @Query("select u from User u "+
            "left join fetch u.organization o "+
            "left join fetch u.roles where u.id= :id")
    Optional<User> findWithRolesById(@Param("id")Long id);

    Boolean existsByEmailAndOrganization_Code(String email, String organizationCode);
    Boolean existsByEmail(String email);

    @Query("select u from User u "+
            "left join fetch u.organization o "+
            "left join fetch u.roles where o.code=:code")
    List<User> findWithRolesByCode(@Param("code") String code);

    @Query("select u from User u "+
            "left join fetch u.organization o "+
            "left join fetch u.roles r where r.name= :role")
    List<User> findWithRolesByRole(@Param("role") ERole role);


    @Query("select u from User u "+
            "left join fetch u.organization o "+
            "left join fetch u.roles "+
            "order by o.name, u.name ")
    List<User> findAllWithRoles();

}
