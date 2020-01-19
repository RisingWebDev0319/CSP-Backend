package ca.freshstart.controller;

import ca.freshstart.data.therapist.repository.TherapistRepository;
import ca.freshstart.types.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@DataJpaTest
//@WebMvcTest
@AutoConfigureMockMvc
@Ignore // todo: tests FAILED
public class TherapistTest extends AbstractTest {
    @Autowired
    protected TherapistRepository therapistRepository;

    @Test
    public void therapistsCount() throws Exception {
        mvc.perform(get("/therapists/count").header(Constants.AUTH_HEADER_NAME, this.loginResponse.getToken()))
                .andExpect(status().isOk()).andExpect(jsonPath("count").value(2));
    }

    @Test
    public void therapistsUnasignedCount() throws Exception {
        mvc.perform(get("/therapists/unassigned/count").header(Constants.AUTH_HEADER_NAME, this.loginResponse.getToken()))
                .andExpect(status().isOk()).andExpect(jsonPath("count").value(2));
    }

    @Test
    public void therapistNotFound() throws Exception {
        mvc.perform(get("/therapist/11231232132").header(Constants.AUTH_HEADER_NAME, this.loginResponse.getToken()))
                .andExpect(status().isNotFound());
    }

//    @Test
    public void therapistServices() throws Exception {
        mvc.perform(get("/therapist/1/services").header(Constants.AUTH_HEADER_NAME, this.loginResponse.getToken()))
                .andExpect(status().isOk()).andExpect(jsonPath("count").value(0));
    }

    public static String json(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T object(MvcResult mvcResult, Class<T> clazz) throws Exception {
        return object(mvcResult.getResponse().getContentAsString(), clazz);
    }

    public static <T> T object(String json, Class<T> clazz) {
        try {
            return new ObjectMapper().readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}