package backend.controller

import backend.Integration.IntegrationTest
import backend.Integration.toJsonNode
import backend.model.event.Event
import backend.model.misc.Coord
import backend.model.user.Admin
import backend.model.user.Participant
import backend.testHelper.asUser
import backend.testHelper.json
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime
import kotlin.test.assertEquals

class TeamControllerTest : IntegrationTest() {

    lateinit var testEvent: Event

    @Before
    override fun setUp() {
        super.setUp()
        this.testEvent = eventService.createEvent("Testevent", LocalDateTime.now(), "Berlin", Coord(0.0, 0.0), 36)
    }

    @Test
    @Ignore
    fun testLeaveTeam() {

    }

    @Test
    @Ignore
    fun testShowInvitationsForUserAndEvent() {

    }

    @Test
    @Ignore
    fun testCreateTeam() {

    }

    @Test
    fun testEditTeamSetHasStartedAsTeamMember() {

        val event = eventService.createEvent("title", LocalDateTime.now(), "Munich", Coord(0.0, 0.0), 36)
        val participant = userService.create("participant@break-out.org", "password", { addRole(Participant::class) })
                .getRole(Participant::class)!!
        val team = teamService.create(participant, "name", "description", event)

        val body = mapOf(
                "hasStarted" to true
        )

        val request = MockMvcRequestBuilders.put("/event/${event.id}/team/${team.id}/")
                .asUser(mockMvc, participant.email, "password")
                .json(body)


        mockMvc.perform(request)
                .andExpect(status().isUnauthorized)
    }

    @Test
    fun testEditTeamSetHasStartedAsAdmin() {
        val event = eventService.createEvent("title", LocalDateTime.now(), "Munich", Coord(0.0, 0.0), 36)
        val participant = userService.create("participant@break-out.org", "password", { addRole(Participant::class) })
                .getRole(Participant::class)!!
        val admin = userService.create("admin@break-out.org", "password", { addRole(Admin::class) })
        val team = teamService.create(participant, "name", "description", event)

        val body = mapOf(
                "hasStarted" to true
        )

        val request = MockMvcRequestBuilders.put("/event/${event.id}/team/${team.id}/")
                .asUser(mockMvc, admin.email, "password")
                .json(body)

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.hasStarted").value(true))

        // Make sure hasStarted gets persisted
        mockMvc.perform(get("/event/${event.id}/team/${team.id}/"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.hasStarted").value(true))
    }

    @Test
    fun aPurposeOfTransferIsCreated() {
        // given an event exists and a participant exist
        val participant = userService.create("test@example.com", "test", { addRole(Participant::class) }).getRole(Participant::class)!!

        // when creating a team
        val body = mapOf(
                "name" to "Testteam",
                "description" to "Beschreibung"
        )

        val request = MockMvcRequestBuilders.post("/event/${testEvent.id}/team/")
                .asUser(mockMvc, participant.email, "test")
                .json(body)

        val response = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .toJsonNode()

        val teamId = response.get("id").intValue

        // then the invoice has a purposeOfTransfer containing the teamId, eventId
        val invoiceRequest = MockMvcRequestBuilders.get("/team/$teamId/startingfee")
                .asUser(mockMvc, participant.email, "test")

        val invoiceResponse = mockMvc.perform(invoiceRequest)
                .andExpect(status().isOk)
                .toJsonNode()

        val invoiceId = invoiceResponse.get("id").intValue

        val expectedPurposeSuffix = "-BREAKOUT${this.testEvent.id}-TEAM$teamId-INVOICE$invoiceId-ENTRYFREE"
        val actualPurpose = invoiceResponse.get("purposeOfTransfer").asText()
        assertEquals(actualPurpose.substring(0, actualPurpose.indexOf("-")).length, 6)
        assert(actualPurpose.endsWith(expectedPurposeSuffix))

    }

    @Test
    @Ignore
    fun testInviteUser() {

    }

    @Test
    @Ignore
    fun testJoinTeam() {

    }

    @Test
    @Ignore
    fun testShowTeam() {

    }

    @Test
    @Ignore
    fun testShowTeamsByEvent() {

    }

    @Test
    @Ignore
    fun testGetTeamPostingIds() {

    }

    @Test
    @Ignore
    fun testGetTeamDistance() {

    }
}
