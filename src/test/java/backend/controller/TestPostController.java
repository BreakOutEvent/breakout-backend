package backend.controller;

import org.junit.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TestPostController extends backend.controller.IntegrationTest {

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
}
