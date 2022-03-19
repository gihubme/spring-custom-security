package eu.nnn4.springjwt2022.payload.request;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationCreateRequest {

    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    @Pattern(regexp = "^[a-z0-9_-]*", message = "must be lower case and not contain special characters")
    @NotBlank
    @Column(nullable = false, unique = true)
    private String code;

    @NotNull
    @Column(nullable = false)
    private Boolean active;

    @NotNull
    @Column(nullable = false)
    private Boolean organizationAllowsUsers;
}
