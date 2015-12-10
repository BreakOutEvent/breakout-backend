package backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TestUserController extends backend.controller.IntegrationTest {

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

}
