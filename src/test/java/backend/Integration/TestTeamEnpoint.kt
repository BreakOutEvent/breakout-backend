package backend.Integration

import backend.model.event.Event
import backend.model.event.EventService
import backend.model.misc.Coords
import backend.model.user.UserService
import org.hamcrest.Matchers.hasSize
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

class TestTeamEnpoint : IntegrationTest() {

    lateinit var event: Event
    lateinit var credentials: Credentials

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var eventService: EventService

    @Before
    override fun setUp() {
        super.setUp()
        event = eventService.createEvent(
                title = "Breakout München",
                date = LocalDateTime.now(),
                city = "Munich",
                startingLocation = Coords(0.0, 0.0),
                duration = 36)
        credentials = createUser(this.mockMvc)
        makeUserParticipant(credentials)
    }

    @Test
    fun testCreateTeam() {

        val body = mapOf("name" to "Team awesome", "description" to "Our team is awesome").toJsonString()

        val request = MockMvcRequestBuilders
                .post("/event/${event.id}/team/")
                .header("Authorization", "Bearer ${credentials.accessToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)

        mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.event").value(event.id!!.toInt()))
                .andExpect(jsonPath("$.name").value("Team awesome"))
                .andExpect(jsonPath("$.description").value("Our team is awesome"))
                .andExpect(jsonPath("$.members").isArray)
                .andExpect(jsonPath<MutableCollection<out Any>>("$.members", hasSize(1)))
    }

    fun failToCreateTeamIfUserIsNoParticipant() {
        val body = mapOf("name" to "Team awesome", "description" to "This team is awesome").toJsonString()

        val request = MockMvcRequestBuilders
                .post("event/${event.id}/team")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)

        mockMvc.perform(request).andExpect(status().isUnauthorized)
    }

    @Test
    fun testGetTeams() {

    }

    @Test
    fun testInviteUser() {

    }

    private fun makeUserParticipant(credentials: Credentials) {

        // Update user with role participant
        val json = mapOf(
                "firstname" to "Florian",
                "lastname" to "Schmidt",
                "gender" to "Male",
                "blocked" to false,
                "participant" to mapOf(
                        "tshirtsize" to "XL",
                        "hometown" to "Dresden",
                        "phonenumber" to "01234567890",
                        "emergencynumber" to "0987654321"
                )
        ).toJsonString()

        val request = MockMvcRequestBuilders
                .put("/user/${credentials.id}/")
                .header("Authorization", "Bearer ${credentials.accessToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(credentials.id))
                .andExpect(jsonPath("$.firstname").value("Florian"))
                .andExpect(jsonPath("$.lastname").value("Schmidt"))
                .andExpect(jsonPath("$.gender").value("Male"))
                .andExpect(jsonPath("$.blocked").value(false))
                .andExpect(jsonPath("$.participant").exists())
                .andExpect(jsonPath("$.participant.tshirtsize").value("XL"))
                .andExpect(jsonPath("$.participant.hometown").value("Dresden"))
                .andExpect(jsonPath("$.participant.phonenumber").value("01234567890"))
                .andExpect(jsonPath("$.participant.emergencynumber").value("0987654321"))
    }

}
