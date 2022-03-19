package eu.nnn4.springjwt2022.event;

import eu.nnn4.springjwt2022.model.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;
import java.util.Date;

@Getter
@Setter
public class OnUserTokenEvent extends OnUserBaseEvent {

    private final String token;

    public OnUserTokenEvent(User user, String token, String organisationName) {
        super(user,organisationName);
        this.token = token;
    }

}
