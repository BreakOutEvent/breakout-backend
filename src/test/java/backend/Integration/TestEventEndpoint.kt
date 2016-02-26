package backend.Integration

import backend.model.user.Admin
import org.junit.Before
import org.junit.Test
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime
import java.time.ZoneOffset

class TestEventEndpoint : IntegrationTest() {

    lateinit var userCredentials: Credentials
    lateinit var adminAccessToken: String

    @Before
    override fun setUp() {
        super.setUp()
        userCredentials = createUser(this.mockMvc, userService = userService)

        userService.create("test_admin@break-out.org", "password", {
            addRole(Admin::class.java)
            isBlocked = false
        })

        adminAccessToken = getTokens(this.mockMvc, "test_admin@break-out.org", "password").first
    }

    @Test
    fun dontCreateEventIfPropertyMissing() {
        val eventData = mapOf(
                "title" to "BreakOut",
                "city" to "München").toJsonString()

        val request = MockMvcRequestBuilders
                .post("/event/")
                .header("Authorization", "Bearer $adminAccessToken")
                .contentType(APPLICATION_JSON)
                .content(eventData)

        mockMvc.perform(request).andExpect(status().isBadRequest)
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
                .header("Authorization", "Bearer $adminAccessToken")
                .contentType(APPLICATION_JSON)
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

    @Test
    fun getAllEvents() {

        createNewEvent()
        createNewEvent()

        // Get Events
        var getEventsRequest = MockMvcRequestBuilders
                .request(HttpMethod.GET, "/event/")

        mockMvc.perform(getEventsRequest)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.[0]").exists())
                .andExpect(jsonPath("$.[1]").exists())
                .andExpect(jsonPath("$.[3]").doesNotExist())
                .andExpect(jsonPath("$.[0].id").exists())
                .andExpect(jsonPath("$.[0].title").exists())
                .andExpect(jsonPath("$.[0].city").exists())
                .andExpect(jsonPath("$.[0].startingLocation.latitude").exists())
                .andExpect(jsonPath("$.[0].startingLocation.longitude").exists())
                .andExpect(jsonPath("$.[0].duration").exists())
                .andExpect(jsonPath("$.[1].id").exists())
                .andExpect(jsonPath("$.[1].title").exists())
                .andExpect(jsonPath("$.[1].city").exists())
                .andExpect(jsonPath("$.[1].startingLocation.latitude").exists())
                .andExpect(jsonPath("$.[1].startingLocation.longitude").exists())
                .andExpect(jsonPath("$.[1].duration").exists())

    }
}
