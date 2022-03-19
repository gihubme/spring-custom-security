package eu.nnn4.springjwt2022.event;

import eu.nnn4.springjwt2022.model.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;
import java.util.Date;

@Getter
@Setter
public class OnUserBaseEvent extends ApplicationEvent {

    private final String userEmail;
    private final String userName;
    private final String organisationName;
    private final Date eventTime;

    public OnUserBaseEvent(User user, String organisationName) {
        super(user.getEmail());
        this.userName=user.getName();
        this.userEmail = user.getEmail();
        this.eventTime = Date.from(Instant.now());
        this.organisationName =organisationName;
    }

}
