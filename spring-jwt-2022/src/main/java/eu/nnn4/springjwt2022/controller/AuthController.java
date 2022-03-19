package eu.nnn4.springjwt2022.controller;

import eu.nnn4.springjwt2022.event.OnUserPasswordChangedEvent;
import eu.nnn4.springjwt2022.event.OnUserPasswordForgotEvent;
import eu.nnn4.springjwt2022.exception.UserApiNotAllowedException;
import eu.nnn4.springjwt2022.payload.request.*;
import eu.nnn4.springjwt2022.event.OnUserSignupEvent;
import eu.nnn4.springjwt2022.exception.ResourceNotFoundException;
import eu.nnn4.springjwt2022.exception.TokenBaseException;
import eu.nnn4.springjwt2022.model.*;
import eu.nnn4.springjwt2022.model.token.ETokenType;
import eu.nnn4.springjwt2022.model.token.TokenBase;
import eu.nnn4.springjwt2022.repository.OrganizationRepository;
import eu.nnn4.springjwt2022.repository.RoleRepository;
import eu.nnn4.springjwt2022.repository.UserRepository;
import eu.nnn4.springjwt2022.payload.response.ApiResponse;
import eu.nnn4.springjwt2022.payload.response.JwtResponse;
import eu.nnn4.springjwt2022.payload.response.UserIdentityAvailability;
import eu.nnn4.springjwt2022.security.CustomAuthenticationToken;
import eu.nnn4.springjwt2022.security.jwt.JwtProvider;
import eu.nnn4.springjwt2022.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.URI;
import java.time.Instant;
import java.util.*;

@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/" + AppUrlConstants.PUBLIC)
public class AuthController {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private AuthenticationManager authenticationManager;
 
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;
 
    @Autowired
    private RoleRepository roleRepository;
 
    @Autowired
    private PasswordEncoder encoder;
 
    @Autowired
    private JwtProvider jwtProvider;
    
    @Autowired
    private TokenService tokenService;
 
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        User user = userRepository.findByEmailAndOrganization_Code(loginRequest.getEmail(),
                        loginRequest.getOrganizationCode())
                .orElseThrow(() -> new ResourceNotFoundException("User","email and companyCode",
                        loginRequest.getEmail()+" and "+ loginRequest.getOrganizationCode()));

        checkUserApiAllowed(user);

        if (user.getActive() && !user.getLocked()) {
            Authentication authentication = authenticationManager.authenticate(
                    new CustomAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword(),
                            loginRequest.getOrganizationCode()
                    )
            );

            Instant now=Instant.now();
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwtToken = jwtProvider.generateJwtTokenFromAuth(authentication,now);
            tokenService.findByUserId(user.getId(),ETokenType.REFRESH)
                .map(TokenBase::getId)
                .ifPresent(tokenService::deleteById);

            TokenBase refreshToken = tokenService.createToken(user,now, ETokenType.REFRESH);
            refreshToken = tokenService.save(refreshToken);
            return ResponseEntity.ok(new JwtResponse(jwtToken, refreshToken.getToken(), jwtProvider.getExpiryDuration()));
    	}
    	return ResponseEntity.badRequest().body(new ApiResponse(false, "User has been deactivated or locked !!"));
    }

    private void checkUserApiAllowed(User user) {
        Role userRole=user.getRoles().stream().filter(role->role.getName().equals(ERole.ROLE_USER)).findAny().orElse(null);
        if(user.getRoles().size()==1 && userRole!=null && !user.getOrganization().getOrganizationAllowsUsers()){
            throw new UserApiNotAllowedException(user.getOrganization().getName());
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
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

        TokenBase emailValidationToken = tokenService.createToken(user,Instant.now(), ETokenType.EMAILVALIDATION);
        emailValidationToken = tokenService.save(emailValidationToken);
        log.info("emailValidationToken was created: "+emailValidationToken.getToken());

        OnUserSignupEvent signupEvent = new OnUserSignupEvent(user,emailValidationToken.getToken(),
                user.getOrganization().getName(),emailValidationToken.getExpiryDate());
        applicationEventPublisher.publishEvent(signupEvent);


        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "User registered successfully!"));
    }

    @GetMapping("/resendValidationEmail")
    public ResponseEntity<?> resendValidationEmail(@Valid @RequestBody IdRelatedRequest request) {
        if(!userRepository.existsByEmailAndOrganization_Code(request.getEmail(),request.getOrganizationCode())) {
            throw new ResourceNotFoundException("User","email and organisation", request.getEmail()+" and "+request.getOrganizationCode());
        }
        User user=userRepository.findByEmailAndOrganization_Code(request.getEmail(),
                request.getOrganizationCode()).get();
        checkUserApiAllowed(user);

        if(!tokenService.findByUserId(user.getId(),ETokenType.EMAILVALIDATION).isPresent()){
            throw new ResourceNotFoundException("Token","userId", user.getId());
        }
        TokenBase tokenOld=tokenService.findByUserId(user.getId(),ETokenType.EMAILVALIDATION).get();
        tokenService.updateEmailValidationToken(tokenOld, Instant.now());
        tokenService.save(tokenOld);
        OnUserSignupEvent signupEvent = new OnUserSignupEvent(user,tokenOld.getToken(),
                user.getOrganization().getName(), tokenOld.getExpiryDate());
        applicationEventPublisher.publishEvent(signupEvent);
        return ResponseEntity.ok("Email was resent");
    }

    @GetMapping(AppUrlConstants.EVALID)
    public ResponseEntity<?> verifyEmail(@RequestParam("token")String token){
        TokenBase evtoken=tokenService.findWithUserByToken(token,ETokenType.EMAILVALIDATION)
                .orElseThrow(()->new ResourceNotFoundException("TokenBase","token",token));
        tokenService.verifyExpiration(evtoken);
        User user=evtoken.getUser();
        checkUserApiAllowed(user);

        user.activate();
        tokenService.deleteById(evtoken.getId());
        user=userRepository.save(user);

        String roleUrl=(user.getRoles().contains("ROLE_USER"))?AppUrlConstants.USER_URL:null;
        roleUrl=(user.getRoles().contains("ROLE_COMPANYUSER"))?AppUrlConstants.COMPANYUSER_URL:roleUrl;

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/signin")
                .build().toUri();

        return ResponseEntity.created(location)
                .body(new ApiResponse(true, "User account was activated!"));
    }

    @PostMapping(AppUrlConstants.PFORGET)
    public ResponseEntity<?> passwordForget(@RequestParam("token")String token, @Valid @RequestBody
            PasswordResetRequest request){
        if(!userRepository.existsByEmailAndOrganization_Code(request.getEmail(),request.getOrganizationCode())) {
            throw new ResourceNotFoundException("User","email and organisation", request.getEmail()+" and "+request.getOrganizationCode());
        }
        if(!request.getPassword().equals(request.getPassword2())){
            throw new IllegalArgumentException("Provided first and second passwords don't matsch.");
        }
        TokenBase ptoken=tokenService.findWithUserByToken(token,ETokenType.PASSWORDFORGET)
                .orElseThrow(()->new ResourceNotFoundException("TokenBase","token",token));
        tokenService.verifyExpiration(ptoken);

        User user=userRepository.findByEmailAndOrganization_Code(request.getEmail(),request.getOrganizationCode()).get();
        checkUserApiAllowed(user);

        if(!ptoken.getUser().equals(user)){
            throw new IllegalArgumentException("Provided token and user don't match");
        }

        user.setPassword(encoder.encode(request.getPassword()));
        userRepository.save(user);
        tokenService.deleteById(ptoken.getId());

        OnUserPasswordChangedEvent passwordForgotEvent = new OnUserPasswordChangedEvent(user,
                user.getOrganization().getName());

        applicationEventPublisher.publishEvent(passwordForgotEvent);
        return ResponseEntity.ok("Password updated and notification email was sent");
    }

    @GetMapping(AppUrlConstants.PFORGET)
    public ResponseEntity<?> passwordForget(@Valid @RequestBody IdRelatedRequest request){
        if(!userRepository.existsByEmailAndOrganization_Code(request.getEmail(),request.getOrganizationCode())) {
            throw new ResourceNotFoundException("User","email and organisation", request.getEmail()+" and "+request.getOrganizationCode());
        }
        User user=userRepository.findByEmailAndOrganization_Code(request.getEmail(),request.getOrganizationCode()).get();
        checkUserApiAllowed(user);

        TokenBase token=tokenService.createToken(user,Instant.now(),ETokenType.PASSWORDFORGET);
        tokenService.save(token);
        OnUserPasswordForgotEvent passwordForgotEvent = new OnUserPasswordForgotEvent(user,token.getToken(),
                user.getOrganization().getName(), token.getExpiryDate());
        applicationEventPublisher.publishEvent(passwordForgotEvent);
        return ResponseEntity.ok("Email was sent");
    }

    @GetMapping(AppUrlConstants.ECHANGESTOP)
    public ResponseEntity<?> emailChangeStop(@RequestParam("token")String token){
        TokenBase evtoken=tokenService.findWithUserByToken(token,ETokenType.EMAILUPDATE)
                .orElseThrow(()->new ResourceNotFoundException("TokenBase","token",token));
        tokenService.verifyExpiration(evtoken);
        User user=evtoken.getUser();
        checkUserApiAllowed(user);
        tokenService.deleteById(evtoken.getId());

        return ResponseEntity.ok("Your registered Email: "+user.getEmail());
    }

    @PatchMapping(AppUrlConstants.ECHANGEVALIDATE)
    public ResponseEntity<?> emailChangeValidation(@RequestParam("token")String token){
        TokenBase evtoken=tokenService.findWithUserByToken(token,ETokenType.EMAILUPDATE)
                .orElseThrow(()->new ResourceNotFoundException("TokenBase","token",token));
        tokenService.verifyExpiration(evtoken);
        User user=evtoken.getUser();
        checkUserApiAllowed(user);
        user.setEmail(evtoken.getNewEmail());
        user=userRepository.save(user);
        tokenService.deleteById(evtoken.getId());
        return ResponseEntity.ok("Your registered Email: "+user.getEmail());
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshJwtToken(@Valid @RequestBody TokenRefreshRequest tokenRefreshRequest) {
    	
    	String requestRefreshToken = tokenRefreshRequest.getRefreshToken();
        Instant now = Instant.now();
    	Optional<String> token = Optional.of(
                tokenService.findWithUserByToken(requestRefreshToken,ETokenType.REFRESH)
                .map(refreshToken -> {
                    tokenService.verifyExpiration(refreshToken);
                    tokenService.updateRefreshToken(refreshToken,now);
                    return refreshToken;
                })
                .map(TokenBase::getUser)
                .map(user->{checkUserApiAllowed(user);return user;})
                .map(u -> jwtProvider.generateJwtTokenFromUser(u,now))
                .orElseThrow(() -> new TokenBaseException(requestRefreshToken, "Missing refresh token in database.Please login again")));

        return ResponseEntity.ok().body(new JwtResponse(token.get(), tokenRefreshRequest.getRefreshToken(), jwtProvider.getExpiryDuration()));
    }

    @GetMapping("/checkEmailAvailability")
    public UserIdentityAvailability checkEmailAvailability(@RequestParam(value = "email") String email,
                                         @RequestParam("code")Optional<String> code) {
        Boolean isAvailable;
        if(code.isPresent()){
            isAvailable = !userRepository.existsByEmailAndOrganization_Code(email,code.get());
        } else {
            isAvailable = !userRepository.existsByEmail(email);
        }
        return new UserIdentityAvailability(isAvailable);
    }



    @GetMapping(value = "/fromip")
    public ResponseEntity<?> getFromp(HttpServletRequest req) {
        String from = _getUserFrom(req);
        return ResponseEntity.ok(from);
    }

//    postman and app not in container  0:0:0:0:0:0:0:1:56729
//    host curl in terminal         192.168.128.1:63386
//    curl within jwt-web container 127.0.0.1:52056
//    postman   192.168.128.1:63380
//    browser   192.168.128.1:63388
    private String _getUserFrom(HttpServletRequest req) {
        String xForwardedFor = req.getHeader("X-Forwarded-For");
        xForwardedFor = xForwardedFor != null && xForwardedFor.contains(",") ? xForwardedFor.split(",")[0]:xForwardedFor;
        String remoteHost = req.getRemoteHost();
        String remoteAddr = req.getRemoteAddr();
        int remotePort = req.getRemotePort();
        StringBuffer sb = new StringBuffer();
        if (remoteHost != null
                && !"".equals(remoteHost)
                && !remoteHost.equals(remoteAddr)) {
            sb.append(remoteHost).append(" ");
        }
        if (xForwardedFor != null
                && !"".equals(xForwardedFor)) {
            sb.append(xForwardedFor).append("(fwd)=>");
        }
        if (remoteAddr != null || !"".equals(remoteAddr)) {
            sb.append(remoteAddr).append(":").append(remotePort);
        }
        return sb.toString();
    }
}

//https://thepracticaldeveloper.com/guide-spring-boot-controller-tests/
//https://rieckpil.de/difference-between-mock-and-mockbean-spring-boot-applications/
//https://www.javachinna.com/disable-spring-security-or-mock-authentication-junit-tests/
//https://stackoverflow.com/questions/15203485/spring-test-security-how-to-mock-authentication
//https://reflectoring.io/spring-boot-testconfiguration/
//https://reflectoring.io/spring-boot-test/