package backend.Integration

import backend.model.event.Event
import backend.model.event.Team
import backend.model.misc.Coords
import backend.model.user.Participant
import backend.model.user.User
import org.hamcrest.Matchers.hasSize
import org.junit.Before
import org.junit.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

class TestTeamEndpoint : IntegrationTest() {

    lateinit var event: Event
    lateinit var team: Team
    lateinit var creatorCredentials: Credentials
    lateinit var creator: User
    lateinit var inviteeCredentials: Credentials
    lateinit var invitee: User

    @Before
    override fun setUp() {
        super.setUp()

        event = eventService.createEvent(
                title = "Breakout München",
                date = LocalDateTime.now(),
                city = "Munich",
                startingLocation = Coords(0.0, 0.0),
                duration = 36)

        creatorCredentials = createUser(this.mockMvc, userService = userService)
        inviteeCredentials = createUser(this.mockMvc, email = "invitee@mail.com", userService = userService)
        makeUserParticipant(creatorCredentials)
        makeUserParticipant(inviteeCredentials)
        creator = userRepository.findOne(creatorCredentials.id.toLong()).getRole(Participant::class.java) as Participant
        invitee = userRepository.findOne(inviteeCredentials.id.toLong())
        team = teamService.create(creator as Participant, "name", "description", event)


    }

    @Test
    fun testCreateTeam() {

        val body = mapOf("name" to "Team awesome", "description" to "Our team is awesome").toJsonString()

        val request = post("/event/${event.id}/team/")
                .header("Authorization", "Bearer ${creatorCredentials.accessToken}")
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

        val request = post("event/${event.id}/team")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)

        mockMvc.perform(request).andExpect(status().isUnauthorized)
    }

    @Test
    fun testGetTeams() {

    }

    @Test
    fun testInviteUser() {
        val body = mapOf("email" to invitee.email).toJsonString()

        val request = post("/event/${event.id}/team/${team.id}/invitation/")
                .header("Authorization", "Bearer ${creatorCredentials.accessToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)

        mockMvc.perform(request)
                .andExpect(status().isCreated)
    }

    @Test
    fun joinTeam() {

        // TODO: Is this a good practice? How to do integration tests...
        testInviteUser()

        val body = mapOf("email" to invitee.email).toJsonString()
        val joinRequest = post("/event/${event.id}/team/${team.id}/member/")
                .header("Authorization", "Bearer ${inviteeCredentials.accessToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)

        // Join team
        mockMvc.perform(joinRequest).andExpect(status().isCreated)
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

        val request = put("/user/${credentials.id}/")
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
