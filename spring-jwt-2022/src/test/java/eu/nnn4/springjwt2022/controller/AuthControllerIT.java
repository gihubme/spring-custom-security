package eu.nnn4.springjwt2022.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.nnn4.springjwt2022.payload.request.LoginRequest;
import eu.nnn4.springjwt2022.payload.response.JwtResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

//@AutoConfigureMockMvc
@Profile("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = {"classpath:application-test.properties"})
class AuthControllerIT {
    private static LoginRequest lr=new LoginRequest("123123");
    private static ObjectMapper mapper=new ObjectMapper();
    private static String jsonRequest;
    private String accessToken;
    @Autowired
    private WebApplicationContext webApplicationContext;
    MockMvc mvc;
    @BeforeAll
    void setUp() throws Exception {
        lr.setEmail("alice@yahoo.com");
        lr.setOrganizationCode("firstorg");
        jsonRequest=mapper.writeValueAsString(lr);
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
//        printApplicationContext();

    }

    void printApplicationContext() {
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        //This
        Arrays.stream(webApplicationContext.getBeanDefinitionNames())
                .map(name -> webApplicationContext.getBean(name).getClass().getName())
                .sorted()
                .forEach(System.out::println);
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
    }

    @Test
    void authenticateUser() throws Exception {
        System.out.println("++++++++++++++++++++++++++++"+(mvc!=null));
        MvcResult result =mvc.perform(
                        MockMvcRequestBuilders
                                .post("/p/signin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonRequest))
                .andExpect(status().isOk()).andReturn();
        String responseJson = result.getResponse().getContentAsString();
        JwtResponse jwtResponse=mapper.readValue(responseJson,JwtResponse.class);
        accessToken=jwtResponse.getAccessToken();
        System.out.println(accessToken+"+++++++++++++++++++++++++++");
    }
}