package ca.freshstart.controller;

import ca.freshstart.applications.auth.types.LoginRequest;
import ca.freshstart.applications.auth.types.LoginResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractTest {
    @Autowired
    protected MockMvc mvc;

    protected LoginResponse loginResponse;

//    @Before
    public void setUp() throws Exception {
        LoginRequest loginRequest = new LoginRequest();

        loginRequest.setUsername("user1@email.com");
        loginRequest.setPassword("pass1");

        String response = mvc
                .perform(post("/login")
                        .content(json(loginRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString();

        this.loginResponse = object(response, LoginResponse.class);
    }

    protected static String json(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static <T> T object(String json, Class<T> clazz) {
        try {
            return new ObjectMapper().readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}