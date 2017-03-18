package backend.Integration

import backend.model.event.Event
import backend.model.misc.Coord
import backend.model.user.Admin
import backend.testHelper.asUser
import backend.testHelper.json
import org.junit.Before
import org.junit.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime
import java.time.ZoneOffset

class TestEventEndpoint : IntegrationTest() {

    lateinit var userCredentials: Credentials
    lateinit var adminAccessToken: String
    lateinit var event: Event


    @Before
    override fun setUp() {
        super.setUp()
        userCredentials = createUser(this.mockMvc, userService = userService)

        userService.create("test_admin@break-out.org", "password", {
            addRole(Admin::class)
            isBlocked = false
        })

        adminAccessToken = getTokens(this.mockMvc, "test_admin@break-out.org", "password").first

        event = eventService.createEvent(
                title = "Breakout München",
                date = LocalDateTime.now(),
                city = "Munich",
                startingLocation = Coord(0.0, 0.0),
                duration = 36)

        eventService.regenerateCache()
    }

    @Test
    fun dontCreateEventIfPropertyMissing() {
        val eventData = mapOf("title" to "BreakOut", "city" to "München")

        val request = post("/event/")
                .asUser(mockMvc, "test_admin@break-out.org", "password")
                .json(eventData)

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
        )

        val request = post("/event/")
                .asUser(mockMvc, "test_admin@break-out.org", "password")
                .json(eventData)

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
        val getEventsRequest = get("/event/")

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

    @Test
    fun getEventById() {

        val event = eventService.createEvent("title", LocalDateTime.now(), "Munich", Coord(0.0, 0.0), 36)

        // Get Events
        val getEventsRequest = get("/event/${event.id}/")

        mockMvc.perform(getEventsRequest)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("title"))
                .andExpect(jsonPath("$.city").value("Munich"))
                .andExpect(jsonPath("$.startingLocation.latitude").exists())
                .andExpect(jsonPath("$.startingLocation.longitude").exists())
                .andExpect(jsonPath("$.duration").exists())
    }


    @Test
    fun testGetEventPostingsById() {
        val request = get("/event/${event.id}/posting/")

        //TODO create Users & Posts
        val response = mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$[0]").doesNotExist())
                .andExpect(jsonPath("$[1]").doesNotExist())
                .andExpect(jsonPath("$[2]").doesNotExist())
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    fun testGetEventDistanceById() {
        val request = get("/event/${event.id}/distance/")

        //TODO check actual values with dummy data
        val response = mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.distance").exists())
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    fun testGetEventDonateSumById() {
        val request = get("/event/${event.id}/donatesum/")

        //TODO check actual values with dummy data
        val response = mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.sponsorSum").exists())
                .andExpect(jsonPath("$.withProofSum").exists())
                .andExpect(jsonPath("$.acceptedProofSum").exists())
                .andExpect(jsonPath("$.fullSum").exists())
                .andReturn().response.contentAsString

        println(response)
    }
}
