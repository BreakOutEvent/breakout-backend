package backend.controller;

import backend.model.user.User;
import backend.model.user.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
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

    //TODO: Testcases where email already exists in database!

    @Test
    public void getAllUsers() throws Exception {
        // Insert dummy users in database
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
                .andExpect(jsonPath("id").exists())
                .andReturn();

        jsonMap = new HashMap<>();
        jsonMap.put("firstname", "Florian");
        jsonMap.put("lastname", "Schmidt");
        jsonMap.put("email", "florianschmidt.1995@icloud.com");
        jsonMap.put("password", "mypassword");
        jsonMap.put("gender", "Male");

        json = om.writeValueAsString(jsonMap);
        this.mockMvc.perform(post("/test/user/", json))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("id").exists())
                .andReturn();


        MvcResult result = this.mockMvc.perform(get("/test/user/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].email").exists())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].blocked").exists())
                .andExpect(jsonPath("$[0].firstname").exists())
                .andExpect(jsonPath("$[0].lastname").exists())
                .andExpect(jsonPath("$[0].password").exists())
                .andExpect(jsonPath("$[0].gender").exists())
                .andExpect(jsonPath("$[0].userRoles").exists())
                .andExpect(jsonPath("$[1].email").exists())
                .andExpect(jsonPath("$[1].id").exists())
                .andExpect(jsonPath("$[1].blocked").exists())
                .andExpect(jsonPath("$[1].firstname").exists())
                .andExpect(jsonPath("$[1].lastname").exists())
                .andExpect(jsonPath("$[1].password").exists())
                .andExpect(jsonPath("$[1].gender").exists())
                .andExpect(jsonPath("$[1].userRoles").exists())
                .andReturn();

        System.out.println(result.getResponse().getContentAsString());
    }
}
