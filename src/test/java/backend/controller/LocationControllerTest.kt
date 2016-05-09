package backend.controller

import backend.Integration.IntegrationTest
import backend.Integration.getTokens
import backend.Integration.toJsonString
import backend.model.event.Event
import backend.model.event.Team
import backend.model.misc.Coord
import backend.model.misc.EmailAddress
import backend.model.user.Participant
import org.hamcrest.Matchers.hasSize
import org.junit.Before
import org.junit.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime
import java.time.ZoneOffset

class LocationControllerTest : IntegrationTest() {

    private lateinit var firstUser: Participant
    private lateinit var thirdUser: Participant
    private lateinit var fifthUser: Participant

    private lateinit var munichEvent: Event
    private lateinit var berlinEvent: Event

    private lateinit var firstTeam: Team
    private lateinit var secondTeam: Team
    private lateinit var thirdTeam: Team

    private lateinit var firstUserToken: String

    @Before
    override fun setUp() {
        super.setUp()

        munichEvent = eventService.createEvent("Event 1", LocalDateTime.now(), "munich", Coord(0.0, 1.1), 36)
        berlinEvent = eventService.createEvent("Event 2", LocalDateTime.now(), "berlin", Coord(1.1, 0.0), 36)

        firstUser = userService.create("test1@break-out.org", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val secondUser = userService.create("test2@break-out.org", "password", { addRole(Participant::class) }).getRole(Participant::class)!!

        thirdUser = userService.create("test3@break-out.org", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val fourthUser = userService.create("test4@break-out.org", "password", { addRole(Participant::class) }).getRole(Participant::class)!!

        fifthUser = userService.create("test5@break-out.org", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val sixthUser = userService.create("test6@break-out.org", "password", { addRole(Participant::class) }).getRole(Participant::class)!!

        firstTeam = teamService.create(firstUser, "team awesome", "description", munichEvent)
        setAuthenticatedUser(firstUser.email)
        teamService.invite(EmailAddress(secondUser.email), firstTeam)
        teamService.join(secondUser, firstTeam)

        secondTeam = teamService.create(thirdUser, "team not awesome", "description", munichEvent)
        setAuthenticatedUser(thirdUser.email)
        teamService.invite(EmailAddress(fourthUser.email), secondTeam)
        teamService.join(fourthUser, secondTeam)

        thirdTeam = teamService.create(fifthUser, "team shit", "description", berlinEvent)
        setAuthenticatedUser(fifthUser.email)
        teamService.invite(EmailAddress(sixthUser.email), thirdTeam)
        teamService.join(sixthUser, thirdTeam)

        firstUserToken = getTokens(mockMvc, firstUser.email, "password").first
    }

    @Test
    fun testGetAllLocationsForEvent() {
        locationService.create(Coord(0.0, 1.1), firstUser, LocalDateTime.now())
        locationService.create(Coord(0.0, 1.1), thirdUser, LocalDateTime.now())
        locationService.create(Coord(0.0, 1.1), fifthUser, LocalDateTime.now()) // This one should not be found!

        val request = MockMvcRequestBuilders.get("/event/${munichEvent.id}/location/")

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath<MutableCollection<out Any>>("$", hasSize(2)))
                .andExpect(jsonPath("$[0].latitude").exists())
                .andExpect(jsonPath("$[0].longitude").exists())
                .andExpect(jsonPath("$[0].team").exists())
                .andExpect(jsonPath("$[0].teamId").exists())
                .andExpect(jsonPath("$[0].eventId").exists())
                .andExpect(jsonPath("$[0].event").exists())
                .andExpect(jsonPath("$[1].team").exists())
                .andExpect(jsonPath("$[1].teamId").exists())
                .andExpect(jsonPath("$[1].eventId").exists())
                .andExpect(jsonPath("$[1].event").exists())
    }

    @Test
    fun testGetAllLocationsForEventAndTeam() {
        locationService.create(Coord(0.0, 1.1), firstUser, LocalDateTime.now())
        locationService.create(Coord(0.0, 1.1), thirdUser, LocalDateTime.now())
        locationService.create(Coord(0.0, 1.1), fifthUser, LocalDateTime.now()) // This one should not be found!

        val request = MockMvcRequestBuilders.get("/event/${munichEvent.id}/team/${firstTeam.id}/location/")

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath<MutableCollection<out Any>>("$", hasSize(1)))
                .andExpect(jsonPath("$[0].latitude").exists())
                .andExpect(jsonPath("$[0].longitude").exists())
                .andExpect(jsonPath("$[0].team").exists())
                .andExpect(jsonPath("$[0].teamId").exists())
                .andExpect(jsonPath("$[0].eventId").exists())
                .andExpect(jsonPath("$[0].event").exists())
    }


    @Test
    fun testCreateLocation() {

        val data = mapOf(
                "latitude" to 0.0,
                "longitude" to 1.1,
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)).toJsonString()

        val request = MockMvcRequestBuilders.post("/event/${munichEvent.id}/team/${firstTeam.id}/location/")
                .header("Authorization", "Bearer $firstUserToken")
                .contentType(APPLICATION_JSON_UTF_8)
                .content(data)

        val resp = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.latitude").exists()) //TODO: Check equality, not only existence
                .andExpect(jsonPath("$.longitude").exists())
                .andExpect(jsonPath("$.team").exists())
                .andExpect(jsonPath("$.teamId").exists())
                .andExpect(jsonPath("$.eventId").exists())
                .andExpect(jsonPath("$.event").exists())
                .andReturn().response.contentAsString

        println(resp)
    }

    @Test
    fun testCreateMultipleLocation() {

        val data = listOf(
                mapOf(
                        "latitude" to 0.0,
                        "longitude" to 1.1,
                        "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)),
                mapOf(
                        "latitude" to 2.2,
                        "longitude" to 3.3,
                        "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
        ).toJsonString()

        println(data)

        val request = MockMvcRequestBuilders.post("/event/${munichEvent.id}/team/${firstTeam.id}/location/multiple/")
                .header("Authorization", "Bearer $firstUserToken")
                .contentType(APPLICATION_JSON_UTF_8)
                .content(data)

        mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath<MutableCollection<out Any>>("$", hasSize(2)))
                .andExpect(jsonPath("$[0].latitude").exists())
                .andExpect(jsonPath("$[0].longitude").exists())
                .andExpect(jsonPath("$[0].team").exists())
                .andExpect(jsonPath("$[0].teamId").exists())
                .andExpect(jsonPath("$[0].eventId").exists())
                .andExpect(jsonPath("$[0].event").exists())
                .andExpect(jsonPath("$[1].team").exists())
                .andExpect(jsonPath("$[1].teamId").exists())
                .andExpect(jsonPath("$[1].eventId").exists())
                .andExpect(jsonPath("$[1].event").exists())
    }
}
