package eu.nnn4.springjwt2022.controller;

import eu.nnn4.springjwt2022.event.OnUserEmailChangeRequestEvent;
import eu.nnn4.springjwt2022.event.OnUserPasswordChangedEvent;
import eu.nnn4.springjwt2022.model.ERole;
import eu.nnn4.springjwt2022.model.token.TokenBase;
import eu.nnn4.springjwt2022.payload.request.EmailUpdateRequest;
import eu.nnn4.springjwt2022.payload.request.LogOutRequest;
import eu.nnn4.springjwt2022.event.OnUserLogoutSuccessEvent;
import eu.nnn4.springjwt2022.exception.ResourceNotFoundException;
import eu.nnn4.springjwt2022.model.User;
import eu.nnn4.springjwt2022.model.token.ETokenType;
import eu.nnn4.springjwt2022.payload.request.PasswordUpdateRequest;
import eu.nnn4.springjwt2022.repository.UserRepository;
import eu.nnn4.springjwt2022.payload.response.ApiResponse;
import eu.nnn4.springjwt2022.payload.response.UserProfile;
import eu.nnn4.springjwt2022.service.CurrentUser;
import eu.nnn4.springjwt2022.service.TokenService;
import eu.nnn4.springjwt2022.service.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/"+AppUrlConstants.API)
public class AccountUpdateController {

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private UserRepository userRepository;
        
    @Autowired
    private TokenService tokenService;
    
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;


    @GetMapping("/me")
    public ResponseEntity<UserProfile> getCurrentUser(@CurrentUser UserPrincipal userPrincipal) {

        User user= userRepository.findWithRolesById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));

        Set<ERole> roles=user.getRoles().stream().map(role->role.getName()).collect(Collectors.toSet());
        UserProfile userProfile = new UserProfile(user.getId(), user.getEmail(), user.getName(),
                user.getActive(), user.getOrganization().getName(),roles);

        return ResponseEntity.ok(userProfile);
    }

    @PutMapping("/logout")
    public ResponseEntity<ApiResponse> logoutUser(@CurrentUser UserPrincipal currentUser,
                                                  @Valid @RequestBody LogOutRequest logOutRequest) {

        tokenService.findByUserId(currentUser.getId(), ETokenType.REFRESH)
                .ifPresent(token-> tokenService.deleteById(token.getId()));

        OnUserLogoutSuccessEvent logoutSuccessEvent = new OnUserLogoutSuccessEvent(currentUser.getEmail(),
                logOutRequest.getToken(), logOutRequest);
        applicationEventPublisher.publishEvent(logoutSuccessEvent);
        return ResponseEntity.ok(new ApiResponse(true, "User has successfully logged out from the system!"));
    }

    @PatchMapping("/updatePassword")
    public ResponseEntity<ApiResponse> updatePassword(@CurrentUser UserPrincipal userPrincipal,
                                                      @RequestBody PasswordUpdateRequest request){
        if(!request.getPassword().equals(request.getPassword2())){
            throw new IllegalArgumentException("Provided first and second passwords don't matsch.");
        }
        User user= userRepository.findWithRolesById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
        user.setPassword(encoder.encode(request.getPassword()));
        String organizationName=user.getOrganization().getName();
        user=userRepository.save(user);
        OnUserPasswordChangedEvent
                passwordChangedEvent = new OnUserPasswordChangedEvent(user, organizationName);
        applicationEventPublisher.publishEvent(passwordChangedEvent);
        return ResponseEntity.ok(new ApiResponse(true, "User has successfully updated password!"));
    }

    @PatchMapping("/updateEmail")
    public ResponseEntity<ApiResponse> updateEmail(@CurrentUser UserPrincipal userPrincipal,
                                                      @RequestBody EmailUpdateRequest request){

        User user= userRepository.findWithRolesById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));

        TokenBase token=tokenService.createToken(user, Instant.now(),ETokenType.EMAILVALIDATION, request.getEmail());
        tokenService.save(token);
        OnUserEmailChangeRequestEvent emailChangeRequestEvent = new OnUserEmailChangeRequestEvent(user,token.getToken(),
                user.getOrganization().getName(), token.getExpiryDate(), request.getEmail());
        applicationEventPublisher.publishEvent(emailChangeRequestEvent);

        return ResponseEntity.ok(new ApiResponse(true, "Emails were sent, please validate new address!"));
    }


}
