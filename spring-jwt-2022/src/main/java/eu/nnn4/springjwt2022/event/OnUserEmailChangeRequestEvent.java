package eu.nnn4.springjwt2022.event;

import eu.nnn4.springjwt2022.model.User;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class OnUserEmailChangeRequestEvent extends OnUserTokenEvent {

    private static final long serialVersionUID = 1L;
    private final Instant emailUpdateExpirationMs;
    private final String newEmail;
    public OnUserEmailChangeRequestEvent(User user, String token, String organisationName,
                                         Instant emailUpdateExpirationMs, String newEmail) {
        super(user, token, organisationName);
        this.emailUpdateExpirationMs=emailUpdateExpirationMs;
        this.newEmail=newEmail;
    }
}
