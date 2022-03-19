package eu.nnn4.springjwt2022.payload.response;

import eu.nnn4.springjwt2022.model.ERole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    private Long id;
    private String email;
    private String name;
    private Boolean active;
    private String organisationName;
    private Set<ERole> roles;
}
