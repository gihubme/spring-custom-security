package eu.nnn4.springjwt2022.event;

import eu.nnn4.springjwt2022.payload.request.LogOutRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;
import java.util.Date;

@Getter
@Setter
public class OnUserLogoutSuccessEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;
	private final String userEmail;
    private final String accessToken;
    private final transient LogOutRequest logOutRequest;
    private final Date eventTime;
    
    public OnUserLogoutSuccessEvent(String userEmail, String accessToken, LogOutRequest logOutRequest) {
        super(userEmail);
        this.userEmail = userEmail;
        this.accessToken = accessToken;
        this.logOutRequest = logOutRequest;
        this.eventTime = Date.from(Instant.now());
    }
}
