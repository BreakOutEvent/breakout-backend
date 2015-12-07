package backend.controller;

import backend.BackendConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BackendConfiguration.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
public class TestPostController {

    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    public void createNewPost() throws Exception {
        //TODO: Check whether Post was really created and saved to database


        //  {
        //      "created": 1388530800000,  // UNIX Timestamp: Wed Jan 01 2014 00:00:00 GMT+0100 (CET)
        //                                 // Date of creation in App, time when user hits "POST" button
        //
        //      "sent": 1388617200000,     // Unix Timestamp: Thu Jan 02 2014 00:00:00 GMT+0100 (CET)
        //                                 // Time when post leaves queue of app, in case app was offline, may be in HTTP post??
        //      "text": "String",          // Content of post
        //      "location" : {
        //          "lat": 51.3,
        //          "lon": 17.2,
        //      },
        //      "challenge_id" : "rand_id" // ID of a challenge that thereby get's fulfilled by the users team!
        //  }
        String json = "{\"created\": 1388530800000,\"sent\": 1388617200000,\"text\": \"String\",\"location\" : {\"lat\": 51.3,\"lon\": 17.2},\"challenge_id\" : \"rand_id\"}";
        mockMvc.perform(post("/test/post/", json))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("id").exists());

    }

    @Test
    public void dontCreatePostForInvalidJSON() throws Exception {
        String missingField = "{\"sent\": 1388617200000,\"text\": \"String\",\"location\" : {\"lat\": 51.3,\"lon\": 17.2},\"challenge_id\" : \"rand_id\"}";
        String missingNestedField = "{\"created\":123456,\"sent\": 1388617200000,\"text\": \"String\",\"location\" : {\"lon\": 17.2},\"challenge_id\" : \"rand_id\"}";
        String invalidJSON = "{#;}\"created\": 1388530800000,\"sent\": 1388617200000,\"text\": \"String\",\"location\" : {\"lat\": 51.3,\"lon\": 17.2},\"challenge_id\" : \"rand_id\"}";

        mockMvc.perform(post("/test/post/", missingField))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/test/post/", invalidJSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/test/post/", missingNestedField))
                .andExpect(status().isBadRequest());
    }

    private MockHttpServletRequestBuilder post(String path, String json) {
        return MockMvcRequestBuilders.post(path)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(json);
    }
}
