package eu.nnn4.springjwt2022.model;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor @AllArgsConstructor
@Entity
public class Organization {
    @Version
    private Integer version;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

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

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }
}
