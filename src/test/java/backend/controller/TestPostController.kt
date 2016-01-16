@file:JvmName("TestPostController")
package backend.controller

import backend.Integration.IntegrationTest
import backend.Integration.toJsonString
import org.junit.Ignore
import org.junit.Test
import org.springframework.http.MediaType

import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import kotlin.collections.forEach
import kotlin.collections.mapOf

class TestPostController : IntegrationTest() {

    @Ignore("Old Code version")
    @Test
    @Throws(Exception::class)
    fun createNewPost() {
        //TODO: Check whether Post was really created and saved to database

        val json = mapOf(
                "created" to 1388530800000,
                "sent" to 1388617200000,
                "text" to "String",
                "location" to mapOf("lat" to 51.3, "lon" to "17.2"),
                "challenge_id" to "rand_id"
        ).toJsonString()

        mockMvc.perform(post("/test/post/", json))
                .andExpect(status().isCreated)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("id").exists())

    }

    @Ignore("Old Code version")
    @Test
    @Throws(Exception::class)
    fun dontCreatePostForInvalidJSON() {

        val missingField = mapOf(
                "sent" to 1388617200000,
                "text" to "String",
                "location" to mapOf("lat" to 51.3, "lon" to 17.2),
                "challenge_id" to "rand_id"
        ).toJsonString()

        val missingNestedField = mapOf(
                "created" to 123456,
                "sent" to  1388617200000,
                "text" to  "String",
                "location"  to  mapOf("lon" to  17.2),
                "challenge_id"  to  "rand_id"
        ).toJsonString()

        val invalidJSON = "{#;}}"

        arrayOf(missingField, missingNestedField, invalidJSON).forEach {
            mockMvc.perform(post("/test/post/", it))
                    .andExpect(status().isBadRequest)
        }

    }
}
