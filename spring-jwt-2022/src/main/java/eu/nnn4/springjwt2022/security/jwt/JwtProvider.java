package eu.nnn4.springjwt2022.security.jwt;

import eu.nnn4.springjwt2022.cache.LoggedOutJwtTokenCache;
import eu.nnn4.springjwt2022.event.OnUserLogoutSuccessEvent;
import eu.nnn4.springjwt2022.exception.InvalidTokenRequestException;
import eu.nnn4.springjwt2022.model.User;
import eu.nnn4.springjwt2022.service.UserPrincipal;
import io.jsonwebtoken.*;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.Date;

@NoArgsConstructor
public class JwtProvider {
 
    private static final Logger log = LoggerFactory.getLogger(JwtProvider.class);

    private JwtProperties jwtProperties;
    private LoggedOutJwtTokenCache loggedOutJwtTokenCache;

    public JwtProvider(JwtProperties jwtProperties,LoggedOutJwtTokenCache loggedOutJwtTokenCache){
        this.jwtProperties=jwtProperties;
        this.loggedOutJwtTokenCache=loggedOutJwtTokenCache;
        loggedOutJwtTokenCache.setJwtProvider(this);
    }

    public String generateJwtTokenFromAuth(Authentication authentication, Instant now) {
        Instant expiryDate = now.plusMillis(jwtProperties.getExpirationMs());
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return buildJwt(userPrincipal.getEmail(),userPrincipal.getOrganizationCode(), userPrincipal.getId(),expiryDate);
    }

    private String buildJwt(String userEmail, String organizationCode, Long userId, Instant expiryDate) {
        return Jwts.builder()
                    .setSubject(userEmail)
                    .setIssuer(organizationCode)
                    .setId(Long.toString(userId))
                    .setIssuedAt(Date.from(Instant.now()))
                    .setExpiration(Date.from(expiryDate))
                    .signWith(SignatureAlgorithm.HS512, jwtProperties.getSecret())
                    .compact();
    }

    public String generateJwtTokenFromUser(User user, Instant now) {
        Instant expiryDate = now.plusMillis(jwtProperties.getExpirationMs());
        return buildJwt(user.getEmail(),user.getOrganization().getCode(),user.getId(), expiryDate);
    }
 
    public Long getUserIdFromJwtToken(String token) {
        String idStr=Jwts.parser()
                      .setSigningKey(jwtProperties.getSecret())
                      .parseClaimsJws(token)
                      .getBody().getId();//.getSubject();
       return Long.parseLong(idStr);
    }
    
    public Date getTokenExpiryFromJwtToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtProperties.getSecret())
                .parseClaimsJws(token)
                .getBody();

        return claims.getExpiration();
    }

    public boolean validateIssuer(String authToken, String issuer){
        try{
            Claims claims=Jwts.parser().setSigningKey(jwtProperties.getSecret()).parseClaimsJws(authToken).getBody();
            log.info("issuer: "+claims.getIssuer()+", subject: "+claims.getSubject());
            return issuer.equals(claims.getIssuer());
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token -> Message: {}", e);
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token -> Message: {}", e);
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token -> Message: {}", e);
        } catch (SignatureException e) {
            log.error("Invalid JWT signature -> Message: {}", e);
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty -> Message: {}", e);
        }
        return false;
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtProperties.getSecret()).parseClaimsJws(authToken);
            validateTokenIsNotForALoggedOutUser(authToken);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token -> Message: {}", e);
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token -> Message: {}", e);
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token -> Message: {}", e);
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty -> Message: {}", e);
        }
        
        return false;
    }
    
    private void validateTokenIsNotForALoggedOutUser(String authToken) {
        OnUserLogoutSuccessEvent previouslyLoggedOutEvent = loggedOutJwtTokenCache.getLogoutEventForToken(authToken);
        if (previouslyLoggedOutEvent != null) {
            String userEmail = previouslyLoggedOutEvent.getUserEmail();
            Date logoutEventDate = previouslyLoggedOutEvent.getEventTime();
            String errorMessage = String.format("Token corresponds to an already logged out user [%s] at [%s]. Please login again", userEmail, logoutEventDate);
            throw new InvalidTokenRequestException("JWT", authToken, errorMessage);
        }
    }
    
    public long getExpiryDuration() {
        return jwtProperties.getExpirationMs();
    }
}
