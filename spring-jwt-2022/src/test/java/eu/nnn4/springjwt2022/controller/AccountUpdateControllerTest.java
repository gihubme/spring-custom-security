package eu.nnn4.springjwt2022.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.nnn4.springjwt2022.cache.LoggedOutJwtTokenCache;
import eu.nnn4.springjwt2022.model.ERole;
import eu.nnn4.springjwt2022.model.User;
import eu.nnn4.springjwt2022.model.token.ETokenType;
import eu.nnn4.springjwt2022.model.token.TokenBase;
import eu.nnn4.springjwt2022.payload.request.LoginRequest;
import eu.nnn4.springjwt2022.payload.response.UserProfile;
import eu.nnn4.springjwt2022.repository.UserRepository;
import eu.nnn4.springjwt2022.security.CustomAuthenticationToken;
import eu.nnn4.springjwt2022.security.CustomUserDetailsAuthenticationProvider;
import eu.nnn4.springjwt2022.security.CustomUserDetailsService;
import eu.nnn4.springjwt2022.security.jwt.JwtProperties;
import eu.nnn4.springjwt2022.security.jwt.JwtProvider;
import eu.nnn4.springjwt2022.service.CustomUserDetailsServiceImpl;
import eu.nnn4.springjwt2022.service.TokenService;
import eu.nnn4.springjwt2022.service.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@TestPropertySource(locations = {"classpath:application-test.properties"})
@WebMvcTest(AccountUpdateController.class)//,secure = false
@ContextConfiguration(classes = {AccountUpdateController.class,
        BCryptPasswordEncoder.class,CustomUserDetailsServiceImpl.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
class AccountUpdateControllerTest {

    private static User user=FakeEntity.getUser("aas@yahoo.com", ERole.ROLE_USER,"org-1");
    private Long expirationMs=600000L;
    private static TokenBase tbase;
    private static String tbaseStr;
    private JwtProperties jwtProperties=new JwtProperties(expirationMs,expirationMs,
            expirationMs,expirationMs,expirationMs,
            "NotEmpty String secret",100, new JwtProperties.Issuer("iss1","iss2"));

    @Spy
    private JwtProvider jwtProvider=new JwtProvider(jwtProperties,
            new LoggedOutJwtTokenCache(100));
    @Autowired private ObjectMapper mapper;
    @Autowired private MockMvc mvc;
    @Autowired private PasswordEncoder encoder;


    @Spy private CustomUserDetailsService userService;

    @MockBean
    private UserRepository userRepository;
    @MockBean
    private TokenService tokenService;
    @InjectMocks
    private AccountUpdateController controller;


    @BeforeEach
    void setUp() {
        Instant inst=Instant.now().plusMillis(expirationMs);
        tbase=new TokenBase(inst, user, ETokenType.REFRESH);
        Authentication auth=new CustomAuthenticationToken(
                user.getEmail(),
                FakeEntity.PASS,
                user.getOrganization().getCode());

        CustomUserDetailsAuthenticationProvider provider=new CustomUserDetailsAuthenticationProvider(encoder,
                userService);
        Mockito.when(userService.loadUserByUsernameAndOrganizationCode(Mockito.anyString(),Mockito.anyString()))
                .thenReturn(UserPrincipal.build(user));

        Authentication auth2=provider.authenticate(auth);
        SecurityContextHolder.getContext().setAuthentication(auth2);
        tbaseStr=jwtProvider.generateJwtTokenFromAuth(auth2,inst);

    }

    @Test
    void getCurrentUser() throws Exception {
        // given
        given(userRepository.findWithRolesById(Mockito.any(Long.class)))
                .willReturn(Optional.of(user));

        // when
        MockHttpServletResponse response = mvc.perform(
                        MockMvcRequestBuilders.get("/api/me")
                                .header("Authorization", "Bearer "+tbaseStr)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(mapper.readValue(response.getContentAsString(), UserProfile.class).getEmail()).isEqualTo(user.getEmail());
    }
}