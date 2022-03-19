package eu.nnn4.springjwt2022.security.jwt;

import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Value
@Validated
@ConfigurationProperties(prefix = "jwt")
@ConstructorBinding
public class JwtProperties {
    @NotNull
    private Long expirationMs;
    @NotNull
    private Long refreshExpirationMs;
    @NotNull
    private Long emailValidationExpirationMs;
    @NotNull
    private Long passwordForgetExpirationMs;
    @NotNull
    private Long emailUpdateExpirationMs;
    @NotEmpty
    private String secret;
    @NotNull
    private Integer loggedOutJwtTokenCacheLimit;

    private final Issuer issuer;

    @Validated
    @Value
    public static class Issuer {
        @NotEmpty
        private String devadmin;
        @NotEmpty
        private String anycompany;
    }

}
