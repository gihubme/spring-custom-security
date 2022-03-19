package eu.nnn4.springjwt2022.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.nnn4.springjwt2022.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class UserPrincipal implements UserDetails {
	private static final long serialVersionUID = 1L;
 
  	private Long id;
 
    private String name;

    private String organizationName;

    private String organizationCode;
 
    private String email;

    private Boolean userActive;

    private Boolean userLocked;

    private Boolean organizationActive;

    private Boolean organizationAllowsUsers;
 
    @JsonIgnore
    private String password;
 
    private Collection<? extends GrantedAuthority> authorities;
    
    private Map<String, Object> attributes;
 
    public UserPrincipal(Long id, String name, String orgName, String orgCode,
              String email, String password, 
              Collection<? extends GrantedAuthority> authorities, Boolean userActive,
                         Boolean userLocked, Boolean companyActive, Boolean organizationAllowsUsers) {
        this.id = id;
        this.name = name;
        this.organizationName=orgName;
        this.organizationCode=orgCode;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.userActive=userActive;
        this.userLocked =userLocked;
        this.organizationActive =companyActive;
        this.organizationAllowsUsers=organizationAllowsUsers;
    }
 
    public static UserPrincipal build(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream().map(role ->
                new SimpleGrantedAuthority(role.getName().name())
        ).collect(Collectors.toList());
 
        return new UserPrincipal(
                user.getId(),
                user.getName(),
                user.getOrganization().getName(),
                user.getOrganization().getCode(),
                user.getEmail(),
                user.getPassword(),
                authorities,
                user.getActive(),
                user.getLocked(),
                user.getOrganization().getActive(),
                user.getOrganization().getOrganizationAllowsUsers()
        );
    }
 
    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getOrganizationName() {
        return organizationName;
    }
    public String getOrganizationCode() {
        return organizationCode;
    }
    public String getEmail() {
        return email;
    }

    public Boolean getUserActive(){return userActive;}
    public Boolean getUserLocked(){return userLocked;}
    public Boolean getOrganizationActive(){return organizationActive;}
    public Boolean getOrganizationAllowsUsers(){return organizationAllowsUsers;}
    
    @Override
	public String getUsername() {
		return email;
	}
 
    @Override
    public String getPassword() {
        return password;
    }
 
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
 
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
 
    @Override
    public boolean isAccountNonLocked() {
        return !this.userLocked;
    }
 
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
 
    @Override
    public boolean isEnabled() {
        return this.userActive && this.organizationActive;
    }
 
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        UserPrincipal user = (UserPrincipal) o;
        return Objects.equals(id, user.id);
    }

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}
}
