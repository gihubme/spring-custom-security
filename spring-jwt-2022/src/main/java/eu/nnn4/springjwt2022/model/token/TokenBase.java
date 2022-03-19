package eu.nnn4.springjwt2022.model.token;

import eu.nnn4.springjwt2022.exception.TokenBaseException;
import eu.nnn4.springjwt2022.model.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@Entity
public class TokenBase implements Serializable {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @OneToOne(optional = false)
    @JoinColumn(name = "USER_ID", unique = true)
    private User user;

    @NotNull
    @Column(name = "TOKEN", nullable = false, unique = true)
    private String token;

    @Column(name = "REFRESH_COUNT")
    private Long refreshCount;

    @NotNull
    @Column(name = "EXPIRY_DT", nullable = false)
    private Instant expiryDate;

    @NotNull
    @Enumerated(value = EnumType.STRING)
    private ETokenType tokenType;

    @Email
    @Column(name = "NEW_EMAIL", nullable = true)
    private String newEmail;

    public TokenBase(Instant expirationMs, User user, ETokenType tokenType){
        this.setExpiryDate(expirationMs);
        this.setToken(UUID.randomUUID().toString());
        this.setRefreshCount(0L);
        this.setUser(user);
        this.tokenType=tokenType;
    }
    public TokenBase(Instant expirationMs, User user, ETokenType tokenType, String newEmail){
        this(expirationMs, user,tokenType);
        this.newEmail=newEmail;
    }

    public void verifyExpiration() {
        if (this.getExpiryDate().compareTo(Instant.now()) < 0) {
            throw new TokenBaseException(this.getToken(), "Expired token. Please issue a new request");
        }
    }

    public void updateToken(Instant expirationMs) {
        this.refreshCount = refreshCount + 1;
        this.expiryDate=expirationMs;
    }

    public void incrementRefreshCount() {
        refreshCount = refreshCount + 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TokenBase)) return false;
        TokenBase that = (TokenBase) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
