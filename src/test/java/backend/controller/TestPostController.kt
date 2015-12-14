@file:JvmName("TestPostController")
package backend.controller

import backend.Integration.IntegrationTest
import org.junit.Test
import org.springframework.http.MediaType

import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class TestPostController : IntegrationTest() {

    @Test
    @Throws(Exception::class)
    fun createNewPost() {
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
        val json = "{\"created\": 1388530800000,\"sent\": 1388617200000,\"text\": \"String\",\"location\" : {\"lat\": 51.3,\"lon\": 17.2},\"challenge_id\" : \"rand_id\"}"
        mockMvc.perform(post("/test/post/", json))
                .andExpect(status().isCreated)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("id").exists())

    }

    @Test
    @Throws(Exception::class)
    fun dontCreatePostForInvalidJSON() {
        val missingField = "{\"sent\": 1388617200000,\"text\": \"String\",\"location\" : {\"lat\": 51.3,\"lon\": 17.2},\"challenge_id\" : \"rand_id\"}"
        val missingNestedField = "{\"created\":123456,\"sent\": 1388617200000,\"text\": \"String\",\"location\" : {\"lon\": 17.2},\"challenge_id\" : \"rand_id\"}"
        val invalidJSON = "{#;}\"created\": 1388530800000,\"sent\": 1388617200000,\"text\": \"String\",\"location\" : {\"lat\": 51.3,\"lon\": 17.2},\"challenge_id\" : \"rand_id\"}"

        mockMvc.perform(post("/test/post/", missingField))
                .andExpect(status().isBadRequest)

        mockMvc.perform(post("/test/post/", invalidJSON))
                .andExpect(status().isBadRequest)

        mockMvc.perform(post("/test/post/", missingNestedField))
                .andExpect(status().isBadRequest)
    }
}
