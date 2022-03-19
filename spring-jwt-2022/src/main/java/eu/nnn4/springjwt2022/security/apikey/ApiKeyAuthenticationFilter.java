package eu.nnn4.springjwt2022.security.apikey;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class ApiKeyAuthenticationFilter  extends OncePerRequestFilter {//implements  Filter

    static final private String HEADERKEY = "DEVSECRET";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if(request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            String apiKey = getApiKey((HttpServletRequest) request);
            String myKey=System.getenv(HEADERKEY);

            if(apiKey != null && myKey!=null) {
                if(apiKey.equals(myKey)) {
                    ApiKeyAuthenticationToken apiToken = new ApiKeyAuthenticationToken(apiKey, AuthorityUtils.NO_AUTHORITIES);
                    SecurityContextHolder.getContext().setAuthentication(apiToken);
                } else {
                    HttpServletResponse httpResponse = (HttpServletResponse) response;
                    httpResponse.setStatus(401);
                    httpResponse.getWriter().write("Invalid API key");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getApiKey(HttpServletRequest httpRequest) {
        String apiKey = null;

        String authHeader = httpRequest.getHeader(HEADERKEY);
        if(authHeader != null) {
            authHeader = authHeader.trim();
            if(authHeader.toUpperCase().startsWith(HEADERKEY + " ")) {
                apiKey = authHeader.substring(HEADERKEY.length()).trim();
            }
        }
        return apiKey;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return !request.getServletPath().startsWith("/d");
    }
}