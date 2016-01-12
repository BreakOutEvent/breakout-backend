package backend.Integration

import org.junit.Before
import org.junit.Test
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.collections.mapOf

class TestEventEndpoint : IntegrationTest() {

    lateinit var userCredentials: Credentials

    @Before
    override fun setUp() {
        super.setUp()
        userCredentials = createUser(this.mockMvc)
    }

    @Test
    fun createNewEvent() {

        val eventData = mapOf(
                "title" to "BreakOut München 2016",
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                "city" to "München",
                "startingLocation" to mapOf(
                        "latitude" to 0.0,
                        "longitude" to 0.0
                ),
                "duration" to 36
        ).toJsonString()

        val request = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/event/")
                .header("Authorization", "Bearer ${userCredentials.accessToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(eventData)

        val response = mockMvc.perform(request)
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.title").exists())
            .andExpect(jsonPath("$.city").exists())
            .andExpect(jsonPath("$.startingLocation.latitude").exists())
            .andExpect(jsonPath("$.startingLocation.longitude").exists())
            .andExpect(jsonPath("$.duration").exists())
            .andReturn().response.contentAsString

        println(response)
    }
}
