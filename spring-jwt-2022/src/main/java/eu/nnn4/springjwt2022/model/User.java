package eu.nnn4.springjwt2022.model;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "users", uniqueConstraints = { @UniqueConstraint(columnNames = { "email" }) })
public class User {
    @Version
    private Integer version;

    @Column(name = "created_date",  updatable = false)
    @CreatedDate
    private Instant createdDate;

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    private Organization organization;

    @NotBlank(message = "User email cannot be null")
    private String email;

    @NotNull(message = "Password cannot be null")
    private String password;

    @NotBlank(message = "Name can not be blank")
    private String name;

    @Column(nullable = false)
    private Boolean active;

    @Column(nullable = false)
    private Boolean locked=false;

    @Size(min = 1)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles", 
      joinColumns = @JoinColumn(name = "user_id"), 
      inverseJoinColumns = @JoinColumn(name = "role_name"))
    private Set<Role> roles = new HashSet<>();

    public void addRole(Role role){ roles.add(role);}
    public void removeRole(Role role){ roles.remove(role);}
    
    public void activate() {
		this.active = true;
	}
	
	public void deactivate() {
		this.active = false;
	}

    public boolean verifiedEmail(){
        return this.active;
    }
}
