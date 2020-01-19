package ca.freshstart.controller;

import ca.freshstart.applications.equipment.EquipmentController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.dom4j.Namespace.get;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@WebMvcTest(EquipmentController.class)
@Ignore
public class EquipmentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EquipmentController equipmentController;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(equipmentController).build();
    }

//    @MockBean
//    private EquipmentRepository equipmentRepository;

    @Test
    public void getVehicleShouldReturnMakeAndModel() throws Exception {
//        given(this.equipmentRepository.findAll())
//                .willReturn(null);

        mockMvc.perform(post("/equipments/count").content(json(null))
                .contentType(MediaType.APPLICATION_JSON).accept(APPLICATION_JSON)
//                .header(Constants.AUTH_HEADER_NAME, this.loginResponse.getToken())
        )
                .andExpect(status().isOk());

//        this.mvc.perform(get("/sboot/vehicle")
//                .accept(null)
//                .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Honda Civic"));
    }

    protected static String json(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
