package ca.freshstart.controller;

import ca.freshstart.data.room.entity.Room;
import ca.freshstart.types.Constants;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.GreaterThan;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Ignore // todo: tests FAILED
public class RoomTest extends AbstractTest {

    @Test
    public void roomsCount() throws Exception {
        mvc
                .perform(get("/rooms/count")
                        .header(Constants.AUTH_HEADER_NAME, this.loginResponse.getToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("count").value(new GreaterThan<>(1)));
    }

    @Test
    public void addRoom() throws Exception {
        Room room = new Room();
        room.setName("123");
        room.setCapacity(2L);

        mvc.perform(post("/room").content(json(room))
                .contentType(MediaType.APPLICATION_JSON).accept(APPLICATION_JSON)
                .header(Constants.AUTH_HEADER_NAME, this.loginResponse.getToken()))
                .andExpect(status().isOk());
    }

    @Test
    public void roomGet() throws Exception {
        mvc.perform(get("/room/2")
                .header(Constants.AUTH_HEADER_NAME, this.loginResponse.getToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value("Room 2"));
    }

    @Test
    public void roomNotFound() throws Exception {
        mvc.perform(get("/room/11231232132")
                .header(Constants.AUTH_HEADER_NAME, this.loginResponse.getToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void roomUpdate() throws Exception {
        Room room = new Room();
        room.setName("Room 1.2");
        room.setCapacity(3L);

        mvc.perform(put("/room/1").content(json(room))
                .contentType(MediaType.APPLICATION_JSON).accept(APPLICATION_JSON)
                .header(Constants.AUTH_HEADER_NAME, this.loginResponse.getToken()))
                .andExpect(status().isOk());

        mvc.perform(get("/room/1")
                .header(Constants.AUTH_HEADER_NAME, this.loginResponse.getToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value(room.getName()))
                .andExpect(jsonPath("capacity").value(room.getCapacity().intValue()));
    }

    @Test
    public void roomDelete() throws Exception {
        mvc.perform(delete("/room/3")
                .header(Constants.AUTH_HEADER_NAME, this.loginResponse.getToken()))
                .andExpect(status().isOk())
                .andExpect(status().reason("Deleted"));
    }
}