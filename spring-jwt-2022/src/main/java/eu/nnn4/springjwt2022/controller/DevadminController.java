package eu.nnn4.springjwt2022.controller;

import eu.nnn4.springjwt2022.event.OnUserCreatedByAdminEvent;
import eu.nnn4.springjwt2022.exception.ResourceNotFoundException;
import eu.nnn4.springjwt2022.exception.UserApiNotAllowedException;
import eu.nnn4.springjwt2022.model.ERole;
import eu.nnn4.springjwt2022.model.Organization;
import eu.nnn4.springjwt2022.model.Role;
import eu.nnn4.springjwt2022.model.User;
import eu.nnn4.springjwt2022.model.token.ETokenType;
import eu.nnn4.springjwt2022.model.token.TokenBase;
import eu.nnn4.springjwt2022.payload.request.OrganizationCreateRequest;
import eu.nnn4.springjwt2022.payload.request.SignupRequest;
import eu.nnn4.springjwt2022.payload.response.ApiResponse;
import eu.nnn4.springjwt2022.repository.OrganizationRepository;
import eu.nnn4.springjwt2022.repository.RoleRepository;
import eu.nnn4.springjwt2022.repository.UserRepository;
import eu.nnn4.springjwt2022.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/" + AppUrlConstants.DEVADMIN_URL)
public class DevadminController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @GetMapping("/organizations")
    public ResponseEntity<List<Organization>> getOrganizations(
            @RequestParam(value = "id", required = false) Optional<Long> id,
            @RequestParam(value = "code", required = false) Optional<String> code) {
        List<Organization> organizations = new ArrayList<>();
        if (id.isPresent()) {
            Organization org = organizationRepository.findById(id.get())
                    .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id.get()));
            organizations.add(org);
        } else if(code.isPresent()){
            Organization org = organizationRepository.findByCode(code.get())
                    .orElseThrow(() -> new ResourceNotFoundException("Organization", "code", code.get()));
            organizations.add(org);
        }
        else {
            organizations = organizationRepository.findAll();
        }
        return ResponseEntity.ok(organizations);
    }

    @PostMapping("/organizations")
    public ResponseEntity<?> getOrganizations(@Valid @RequestBody OrganizationCreateRequest request) {
        Organization organization = new Organization();
        organization.setName(request.getName());
        organization.setCode(request.getCode());
        organization.setOrganizationAllowsUsers(request.getOrganizationAllowsUsers());
        organization.setActive(request.getActive());
        organizationRepository.save(organization);
        return ResponseEntity.ok("New organisation was created");
    }

    @PatchMapping("/organizations/{id}/deactivate")
    public ResponseEntity<ApiResponse> deactivateOrganizationById(@PathVariable(value = "id") Long id) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));
        org.deactivate();
        organizationRepository.save(org);
        return ResponseEntity.ok(new ApiResponse(true, "Organization deactivated successfully!"));
    }

    @PatchMapping("/organizations/{id}/activate")
    public ResponseEntity<ApiResponse> activateOrganizationById(@PathVariable(value = "id") Long id) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));
        org.activate();
        organizationRepository.save(org);
        return ResponseEntity.ok(new ApiResponse(true, "Organization activated successfully!"));
    }

    @PatchMapping("/organizations/{id}/allowUsers")
    public ResponseEntity<ApiResponse> organizationByIdAllowUsers(@PathVariable(value = "id") Long id) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));
        org.setOrganizationAllowsUsers(true);
        organizationRepository.save(org);
        return ResponseEntity.ok(new ApiResponse(true, "Organization allows user api!"));
    }

    @PatchMapping("/organizations/{id}/disallowUsers")
    public ResponseEntity<ApiResponse> organizationByIdDisallowUsers(@PathVariable(value = "id") Long id) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));
        org.setOrganizationAllowsUsers(false);
        organizationRepository.save(org);
        return ResponseEntity.ok(new ApiResponse(true, "Organization doesn't  allow user api!"));
    }


    @GetMapping("/users")
    public ResponseEntity<List<User>> getUsers(
                @RequestParam(value = "id", required = false) Optional<Long> id,
                @RequestParam(value = "code", required = false) Optional<String> code,
                @RequestParam(value = "role", required = false) Optional<ERole> role                              ) {
        List<User> users = new ArrayList<>();
        if (id.isPresent()) {
            User user = userRepository.findWithRolesById(id.get())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", id.get()));
            users.add(user);
        } else if(code.isPresent() || role.isPresent()) {
            if (role.isPresent()) {
                users = userRepository.findWithRolesByRole(role.get());
            }
            if (code.isPresent()) {
                if(users.size()>0){
                    List<User> toRemove=users.stream()
                            .filter(u->!u.getOrganization().getCode().equals(code.get()))
                            .collect(Collectors.toList());
                    users.removeAll(toRemove);
                }else{
                    users = userRepository.findWithRolesByCode(code.get());
                }
            }
        } else {
            users = userRepository.findAllWithRoles();
        }
        return ResponseEntity.ok(users);
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@Valid @RequestBody SignupRequest signUpRequest){
        if(userRepository.existsByEmailAndOrganization_Code(signUpRequest.getEmail(),signUpRequest.getOrganizationCode())) {
            return new ResponseEntity<String>("Fail -> Email is already in use!",
                    HttpStatus.BAD_REQUEST);
        }
        if(!organizationRepository.findByCode(signUpRequest.getOrganizationCode()).isPresent()) {
            throw new ResourceNotFoundException("Organization","code",signUpRequest.getOrganizationCode());
        }
        Organization organization=organizationRepository.findByCode(signUpRequest.getOrganizationCode()).get();
        User user = new User();
        user.setOrganization(organization);
        user.setName(signUpRequest.getName());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        strRoles.forEach(role -> {
            try{
                ERole erole=ERole.valueOf(role);
                switch (erole) {
                    case ROLE_ADMIN:
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", erole.name()));
                        roles.add(adminRole);

                        break;
                    case ROLE_COMPANYUSER:
                        Role companyuserRole = roleRepository.findByName(ERole.ROLE_COMPANYUSER)
                                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", erole.name()));
                        roles.add(companyuserRole);

                        break;
                    case ROLE_USER:
                        if(organization.getOrganizationAllowsUsers()){
                            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                    .orElseThrow(() -> new ResourceNotFoundException("Role", "name", erole.name()));
                            roles.add(userRole);
                        }
                        break;
                    default:
                        throw new ResourceNotFoundException("Role", "name", role);
                }
            }catch (IllegalArgumentException e){
                throw new ResourceNotFoundException("Role", "name", role);
            }
        });
        if(roles.size()==0){
            throw new UserApiNotAllowedException(organization.getName());
        }
        user.setRoles(roles);
        user.deactivate();
        User savedUser = userRepository.save(user);

        TokenBase emailValidationToken = tokenService.createToken(user, Instant.now(), ETokenType.EMAILVALIDATION);
        emailValidationToken = tokenService.save(emailValidationToken);
        log.info("emailValidationToken was created: "+emailValidationToken.getToken());

        OnUserCreatedByAdminEvent event = new OnUserCreatedByAdminEvent( signUpRequest.getPassword(),
                user,user.getOrganization().getName());
        applicationEventPublisher.publishEvent(event);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "User created successfully!"));
    }

    @PatchMapping("/users/{id}/deactivate")
    public ResponseEntity<ApiResponse> deactivateUserById(@PathVariable(value = "id") Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.deactivate();
        userRepository.save(user);
        return ResponseEntity.ok(new ApiResponse(true, "User deactivated successfully!"));
    }

    @PatchMapping("/users/{id}/activate")
    public ResponseEntity<ApiResponse> activateUserById(@PathVariable(value = "id") Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.activate();
        userRepository.save(user);
        return ResponseEntity.ok(new ApiResponse(true, "User activated successfully!"));
    }

    @DeleteMapping("/users/{id}/delete")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable(value = "id") Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        userRepository.delete(user);
        return ResponseEntity.ok(new ApiResponse(true, "User deleted successfully!"));
    }


    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getRoles() {
        List<Role> roles = roleRepository.findAll();
        return ResponseEntity.ok(roles);
    }
}
