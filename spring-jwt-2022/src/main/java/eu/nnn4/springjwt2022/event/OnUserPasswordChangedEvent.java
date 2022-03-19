package eu.nnn4.springjwt2022.event;

import eu.nnn4.springjwt2022.model.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OnUserPasswordChangedEvent  extends OnUserBaseEvent {
    private static final long serialVersionUID = 1L;
    public OnUserPasswordChangedEvent(User user, String organisationName) {
        super(user,organisationName);
    }
}
