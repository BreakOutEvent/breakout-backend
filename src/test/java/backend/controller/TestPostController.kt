@file:JvmName("TestPostController")

package backend.controller

import backend.Integration.Credentials
import backend.Integration.IntegrationTest
import backend.Integration.createUser
import backend.Integration.toJsonString
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.collections.forEach
import kotlin.collections.mapOf

class TestPostController : IntegrationTest() {

    lateinit var userCredentials: Credentials

    @Before
    override fun setUp() {
        super.setUp()
        userCredentials = createUser(this.mockMvc)
    }

    @Test
    @Throws(Exception::class)
    fun createNewPost() {
        val postData = mapOf(
                "text" to "TestPost",
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                "postLocation" to mapOf(
                        "latitude" to 0.0,
                        "longitude" to 0.0
                )
        ).toJsonString()

        val request = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/post/")
                .header("Authorization", "Bearer ${userCredentials.accessToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.text").exists())
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.user").exists())
                .andExpect(jsonPath("$.postLocation.latitude").exists())
                .andExpect(jsonPath("$.postLocation.longitude").exists())
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    @Throws(Exception::class)
    fun dontCreatePostForInvalidJSON() {
        val postData = mapOf(
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                "postLocation" to mapOf(
                        "latitude" to 0.0,
                        "longitude" to 0.0
                )
        ).toJsonString()

        val request = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/post/")
                .header("Authorization", "Bearer ${userCredentials.accessToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isBadRequest)
                .andReturn().response.contentAsString

        println(response)
    }


    @Test
    @Throws(Exception::class)
    fun dontCreatePostWithoutValidAuth() {
        val postData = mapOf(
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                "postLocation" to mapOf(
                        "latitude" to 0.0,
                        "longitude" to 0.0
                )
        ).toJsonString()

        val request = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/post/")
                .header("Authorization", "Bearer invalidToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isUnauthorized)
                .andReturn().response.contentAsString

        println(response)
    }
}