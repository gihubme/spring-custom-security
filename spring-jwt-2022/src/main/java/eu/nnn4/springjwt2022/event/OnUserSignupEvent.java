package eu.nnn4.springjwt2022.event;

import eu.nnn4.springjwt2022.model.User;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class OnUserSignupEvent extends OnUserTokenEvent {

	private static final long serialVersionUID = 1L;
    private final Instant emailValidationExpirationMs;

    public OnUserSignupEvent(User user, String token, String organisationName,Instant emailValidationExpirationMs) {
        super(user, token, organisationName);
        this.emailValidationExpirationMs=emailValidationExpirationMs;
    }
}
