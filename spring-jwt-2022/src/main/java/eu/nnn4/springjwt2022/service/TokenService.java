package eu.nnn4.springjwt2022.service;

import eu.nnn4.springjwt2022.config.PropertyresolutionHelper;
import eu.nnn4.springjwt2022.controller.AppUrlConstants;
import eu.nnn4.springjwt2022.model.User;
import eu.nnn4.springjwt2022.model.token.ETokenType;
import eu.nnn4.springjwt2022.model.token.TokenBase;
import eu.nnn4.springjwt2022.repository.TokenBaseRepository;
import eu.nnn4.springjwt2022.security.jwt.JwtProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.Optional;

@Service
public class TokenService {

    private JwtProperties jwtProperties;
    private TokenBaseRepository tokenRepository;

    @Autowired
    public TokenService(TokenBaseRepository tokenRepository,
                        JwtProperties jwtProperties ){
        this.tokenRepository =tokenRepository;
        this.jwtProperties=jwtProperties;
    }


    public Optional<TokenBase> findWithUserByToken(String token,ETokenType tokenType) {
        return tokenRepository.findWithUserByTokenAndTokenType(token,tokenType);
    }

    public Optional<TokenBase> findByToken(String token,ETokenType tokenType) {
        return tokenRepository.findByTokenAndTokenType(token,tokenType);
    }

    public Optional<TokenBase> findByUserId(Long userId,ETokenType tokenType){
        return tokenRepository.findByTokenTypeAndUser_Id(tokenType,userId);
    }

    public TokenBase save(TokenBase token) {
        return tokenRepository.save(token);
    }
    
    public TokenBase createToken(User user, Instant now, ETokenType tokenType) {
        Instant expirationMs=null;
        switch(tokenType){
            case REFRESH:
                expirationMs=now.plusMillis(jwtProperties.getRefreshExpirationMs());
                break;
            case EMAILVALIDATION:
                expirationMs=now.plusMillis(jwtProperties.getEmailValidationExpirationMs());
                break;
            case PASSWORDFORGET:
                expirationMs=now.plusMillis(jwtProperties.getPasswordForgetExpirationMs());
                break;
            default:
                throw new IllegalArgumentException("Token generation for the provided tokenType is not implemented.");
        }

        TokenBase token = new TokenBase(expirationMs, user,tokenType);
        return token;
    }

    public TokenBase createToken(User user, Instant now, ETokenType tokenType, String newEmail) {
        Instant expirationMs=null;
        switch(tokenType){
            case EMAILUPDATE:
                expirationMs=now.plusMillis(jwtProperties.getRefreshExpirationMs());
                break;
            default:
                throw new IllegalArgumentException("Token generation for the provided tokenType is not implemented.");
        }

        TokenBase token = new TokenBase(expirationMs, user,tokenType, newEmail);
        return token;
    }

    public void verifyExpiration(TokenBase token) {
        token.verifyExpiration();
    }

    public void deleteById(Long id) {
        tokenRepository.deleteById(id);
    }

    public void updateRefreshToken(TokenBase refreshToken, Instant now) {
        Instant expirationMs=now.plusMillis(jwtProperties.getRefreshExpirationMs());
        updateToken(refreshToken,expirationMs);
    }

    public void updateEmailValidationToken(TokenBase emailValidationToken, Instant now) {
        Instant expirationMs=now.plusMillis(jwtProperties.getEmailValidationExpirationMs());
        updateToken(emailValidationToken,expirationMs);
    }

    private void updateToken(TokenBase refreshToken,Instant expirationMs){
        refreshToken.updateToken(expirationMs);
        save(refreshToken);
    }

    public URI getLink(TokenBase mtoken, @Autowired Environment env){
        Integer mport=null;
        String mhost=null;

        try{
            mhost=PropertyresolutionHelper.getProperty("HOST",env).toString();
            mport=(!"localhost".equals(mport))?null:Integer.parseInt(PropertyresolutionHelper.getProperty("PORT",env).toString());
        }catch (Exception ex){
            throw new IllegalArgumentException("Couldn't resolve uri parameters");
        }

        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme("http").host(mhost) //"www.google.com"
                .port(mport)
                .path("/"+ AppUrlConstants.EMAILVALIDATION)
                .query("token={mtoken}")
                .buildAndExpand(mtoken.getToken()).encode();
        return uriComponents.toUri();
    }
}
