package eu.nnn4.springjwt2022.config;

import eu.nnn4.springjwt2022.cache.LoggedOutJwtTokenCache;
import eu.nnn4.springjwt2022.security.jwt.JwtProperties;
import eu.nnn4.springjwt2022.security.jwt.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;

@EnableConfigurationProperties(JwtProperties.class)
@Configuration
public class JwtConfiguration {

    @Autowired
    private JwtProperties jwtProperties;

    @DependsOn("loggedOutJwtTokenCache")
    @Order(101)
    @Bean
    public JwtProvider jwtProvider(LoggedOutJwtTokenCache loggedOutJwtTokenCache){
        return new JwtProvider(jwtProperties,loggedOutJwtTokenCache);
    }

    @Order(100)
    @Bean
    public LoggedOutJwtTokenCache loggedOutJwtTokenCache(){
        return new LoggedOutJwtTokenCache(jwtProperties.getLoggedOutJwtTokenCacheLimit());
    }
}
