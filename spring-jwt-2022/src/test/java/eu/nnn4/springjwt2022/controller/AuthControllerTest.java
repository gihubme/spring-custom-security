package eu.nnn4.springjwt2022.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.nnn4.springjwt2022.advice.RestApiControllerAdvice;
import eu.nnn4.springjwt2022.cache.LoggedOutJwtTokenCache;
import eu.nnn4.springjwt2022.model.ERole;
import eu.nnn4.springjwt2022.model.User;
import eu.nnn4.springjwt2022.model.token.ETokenType;
import eu.nnn4.springjwt2022.model.token.TokenBase;
import eu.nnn4.springjwt2022.payload.request.LoginRequest;
import eu.nnn4.springjwt2022.payload.response.JwtResponse;
import eu.nnn4.springjwt2022.repository.TokenBaseRepository;
import eu.nnn4.springjwt2022.repository.UserRepository;
import eu.nnn4.springjwt2022.security.CustomAuthenticationToken;
import eu.nnn4.springjwt2022.security.CustomUserDetailsAuthenticationProvider;
import eu.nnn4.springjwt2022.security.CustomUserDetailsService;
import eu.nnn4.springjwt2022.security.jwt.JwtProperties;
import eu.nnn4.springjwt2022.security.jwt.JwtProvider;
import eu.nnn4.springjwt2022.service.TokenService;
import eu.nnn4.springjwt2022.service.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//test controller using MockMVC with Standalone setup. No Spring context
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mvc;
    private static User user=FakeEntity.getUser("aas@yahoo.com",ERole.ROLE_USER,"org-1");
    private static ObjectMapper mapper=new ObjectMapper();
    private Long expirationMs=600000L;
    private JwtProperties jwtProperties=new JwtProperties(expirationMs,expirationMs,
            expirationMs,expirationMs,expirationMs,
            "NotEmpty String secret",100, new JwtProperties.Issuer("iss1","iss2"));
    LoginRequest loginRequest = new LoginRequest(FakeEntity.PASS,user.getEmail(), user.getOrganization().getCode());

    @Spy
    private JwtProvider jwtProvider=new JwtProvider(jwtProperties,
            new LoggedOutJwtTokenCache(100));

    @Mock
    private UserRepository userRepository;
    @Spy
    private CustomUserDetailsService userService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthController authController;


    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new RestApiControllerAdvice())
//                .addFilters(new CustomAuthenticationFilter())
                .build();
        Authentication auth=new CustomAuthenticationToken(
                user.getEmail(),
                user.getPassword(),
                user.getOrganization().getCode());
        Instant inst=Instant.now().plusMillis(jwtProperties.getRefreshExpirationMs());
        TokenBase tbase=new TokenBase(inst, user,ETokenType.REFRESH);

        Mockito.when(userService.loadUserByUsernameAndOrganizationCode(Mockito.anyString(),Mockito.anyString()))
                .thenReturn(UserPrincipal.build(user));
        Mockito.when(passwordEncoder.matches(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(userRepository.findByEmailAndOrganization_Code(user.getEmail(), user.getOrganization().getCode()))
                .thenReturn(Optional.of(user));

        Mockito.when(userService
                .loadUserByUsernameAndOrganizationCode(user.getEmail(), user.getOrganization().getCode()))
        .thenReturn(UserPrincipal.build(user));


        CustomUserDetailsAuthenticationProvider provider=new CustomUserDetailsAuthenticationProvider(passwordEncoder,
                userService);
        Authentication auth2=provider.authenticate(auth);
        Mockito.when(authenticationManager
                        .authenticate(Mockito.any(CustomAuthenticationToken.class)))
        .thenReturn(auth2);


        Mockito.when(tokenService.createToken(Mockito.any(User.class),
                Mockito.any(Instant.class), Mockito.any(ETokenType.class)))
                .thenReturn(tbase);
        Mockito.when(tokenService.save(tbase))
                .thenReturn(tbase);
        Mockito.when(tokenService.findByUserId(Mockito.any(Long.class),Mockito.any(ETokenType.class)))
                .thenReturn(Optional.empty());

    }

    @Test
    void authenticateUser() throws Exception {
        // given
        given(userRepository.findByEmailAndOrganization_Code(user.getEmail(), user.getOrganization().getCode()))
                .willReturn(Optional.of(user));

        // when
        MockHttpServletResponse response = mvc.perform(
                        MockMvcRequestBuilders.post("/p/signin")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(loginRequest)))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).contains("accessToken");

    }

    @Test
    public void testAuthenticateUser() throws Exception {

        MockHttpServletResponse response = mvc.perform(MockMvcRequestBuilders
                        .post("/p/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .content(mapper.writeValueAsString(loginRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
//                        .value("true"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn().getResponse();

        JwtResponse jwtResponse=mapper.readValue(response.getContentAsString(),JwtResponse.class);
        String accessToken=jwtResponse.getAccessToken();
        System.out.println(accessToken+"+++++++++++++++++++++++++++");
    }


}