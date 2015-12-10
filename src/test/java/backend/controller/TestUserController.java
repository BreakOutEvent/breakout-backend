package backend.controller;

import backend.model.user.User;
import backend.model.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TestUserController extends backend.controller.IntegrationTest {

    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    public void addUser() throws Exception {

        Map<String, String> jsonMap = new HashMap<>();
        jsonMap.put("firstname", "Florian");
        jsonMap.put("lastname", "Schmidt");
        jsonMap.put("email", "florian.schmidt.1994@icloud.com");
        jsonMap.put("password", "mypassword");
        jsonMap.put("gender", "Male");

        ObjectMapper om = new ObjectMapper();
        String json = om.writeValueAsString(jsonMap);

        this.mockMvc.perform(post("/test/user/", json))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("id").exists());
    }

    @Test
    public void dontCreateUserForInvalidBody() throws Exception {

        // firstname is missing
        Map<String, String> jsonMap = new HashMap<>();
        jsonMap.put("lastname", "Schmidt");
        jsonMap.put("email", "florian.schmidt.1994@icloud.com");
        jsonMap.put("password", "mypassword");
        jsonMap.put("gender", "Male");

        ObjectMapper om = new ObjectMapper();
        String json = om.writeValueAsString(jsonMap);

        this.mockMvc.perform(post("/test/user/", json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void userGetsSavedToDatabase() throws Exception {
        Map<String, String> jsonMap = new HashMap<>();
        jsonMap.put("firstname", "Florian");
        jsonMap.put("lastname", "Schmidt");
        jsonMap.put("email", "florianschmidt.1994@icloud.com");
        jsonMap.put("password", "mypassword");
        jsonMap.put("gender", "Male");

        ObjectMapper om = new ObjectMapper();
        String json = om.writeValueAsString(jsonMap);

        MvcResult result = this.mockMvc.perform(post("/test/user/", json))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("id").exists())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        User user = userRepository.findByEmail("florianschmidt.1994@icloud.com");
        assertNotNull(user);
        assertEquals(user.getEmail(), jsonMap.get("email"));

    }

}
