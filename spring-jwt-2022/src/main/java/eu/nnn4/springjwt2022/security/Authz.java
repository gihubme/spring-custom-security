package eu.nnn4.springjwt2022.security;

import eu.nnn4.springjwt2022.controller.AppUrlConstants;
import eu.nnn4.springjwt2022.service.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Component
public class Authz {

    public boolean isMember(String organizationCode) {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth!=null &&  ((UserPrincipal)auth.getPrincipal()).getOrganizationCode().equals(organizationCode);
    }

    public boolean isApiAllowed(String roleUrlPart) {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(roleUrlPart.equals(AppUrlConstants.USER_URL)){
            return  auth!=null && ((UserPrincipal)auth.getPrincipal()).getOrganizationAllowsUsers();
        }
        return true;
    }

    private String getIssuer(HttpServletRequest request) {
        if(request.getServletPath().startsWith("/api")){
            return request.getServletPath().split("/")[1];
        }
        return null;
    }
}
