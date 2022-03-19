package eu.nnn4.springjwt2022.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@Setter
@ResponseStatus(HttpStatus.EXPECTATION_FAILED)
public class UserApiNotAllowedException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private final String organisationName;

    public UserApiNotAllowedException(String organisationName) {
        super(String.format("Organisation - [%s] disabled its user API)", organisationName));
        this.organisationName = organisationName;
    }
}
