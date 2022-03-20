package eu.nnn4.springjwt2022.controller;

import eu.nnn4.springjwt2022.exception.ResourceNotFoundException;
import eu.nnn4.springjwt2022.model.User;
import eu.nnn4.springjwt2022.repository.UserRepository;
import eu.nnn4.springjwt2022.service.CurrentUser;
import eu.nnn4.springjwt2022.service.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/"+AppUrlConstants.API
+"/{"+ AppUrlConstants.COMPANYCODE + "}/{" + AppUrlConstants.ROLEMAP + "}")
//SpEL will work only with arg variables named `code` and `roleMap`
//@PreAuthorize("@authz.isMember(#code) && @authz.isApiAllowed(#roleMap)")
public class TestController {
    @Autowired
    private UserRepository userRepository;

    @PreAuthorize("@authz.isMember(#code) && @authz.isApiAllowed(#roleMap)")
    @GetMapping("/test")
    public ResponseEntity<?> test(@CurrentUser UserPrincipal userPrincipal,
                                  @Valid @PathVariable(AppUrlConstants.COMPANYCODE) String code,
                                  @Valid @PathVariable(AppUrlConstants.ROLEMAP) String roleMap){
        User user= userRepository.findWithRolesById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
        return ResponseEntity.ok("name: "+user.getName()
                +", code: "+user.getOrganization().getCode()
                +", role: "+user.getRoles().stream().findFirst().get().getName());
    }
}
