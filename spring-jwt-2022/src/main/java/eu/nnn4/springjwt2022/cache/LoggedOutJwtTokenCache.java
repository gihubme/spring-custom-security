package eu.nnn4.springjwt2022.cache;

import eu.nnn4.springjwt2022.event.OnUserLogoutSuccessEvent;
import eu.nnn4.springjwt2022.security.jwt.JwtProvider;
import net.jodah.expiringmap.ExpiringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

//@Component
public class LoggedOutJwtTokenCache {
	private static final Logger logger = LoggerFactory.getLogger(LoggedOutJwtTokenCache.class);

    private ExpiringMap<String, OnUserLogoutSuccessEvent> tokenEventMap;
    private JwtProvider tokenProvider;

    public LoggedOutJwtTokenCache(int limit) {
        this.tokenEventMap = ExpiringMap.builder()
                .variableExpiration()
                .maxSize(limit)
                .build();
    }
    public void setJwtProvider(JwtProvider tokenProvider){
        this.tokenProvider=tokenProvider;
    }
    public void markLogoutEventForToken(OnUserLogoutSuccessEvent event) {
        String token = event.getAccessToken();
        if (tokenEventMap.containsKey(token)) {
            logger.info(String.format("Log out token for user [%s] is already present in the cache", event.getUserEmail()));

        } else {
            Date tokenExpiryDate = tokenProvider.getTokenExpiryFromJwtToken(token);
            long ttlForToken = getTTLForToken(tokenExpiryDate);
            logger.info(String.format("Logout token cache set for [%s] with a TTL of [%s] seconds. Token is due expiry at [%s]", event.getUserEmail(), ttlForToken, tokenExpiryDate));
            tokenEventMap.put(token, event, ttlForToken, TimeUnit.SECONDS);
        }
    }

    public OnUserLogoutSuccessEvent getLogoutEventForToken(String token) {
        return tokenEventMap.get(token);
    }

    private long getTTLForToken(Date date) {
        long secondAtExpiry = date.toInstant().getEpochSecond();
        long secondAtLogout = Instant.now().getEpochSecond();
        return Math.max(0, secondAtExpiry - secondAtLogout);
    }
}
