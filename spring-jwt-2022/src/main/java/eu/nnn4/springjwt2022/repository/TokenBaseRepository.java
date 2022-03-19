package eu.nnn4.springjwt2022.repository;

import eu.nnn4.springjwt2022.model.token.ETokenType;
import eu.nnn4.springjwt2022.model.token.TokenBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenBaseRepository extends JpaRepository<TokenBase, Long> {
    Optional<TokenBase> findById(Long id);
    Optional<TokenBase> findByTokenAndTokenType(String token, ETokenType tokenType);
    Optional<TokenBase> findByTokenTypeAndUser_Id(ETokenType tokenType,Long id);

    @Query("select u from #{#entityName} u "+
            "left join fetch u.user u2 "+
            "left join fetch u2.roles "+
            "left join fetch u2.organization o "+
            "where u.token= :token and u.tokenType= :tokenType")
    Optional<TokenBase> findWithUserByTokenAndTokenType(@Param("token") String token,
                                                        @Param("tokenType") ETokenType tokenType);
}

//@NoRepositoryBean
//public interface TokenBaseRepository<T extends TokenBase, ID extends Long> extends JpaRepository<T, ID>
//@Query("select u from #{#entityName} u left join fetch u.user where u.token= :token")