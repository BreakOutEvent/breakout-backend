package backend.controller

import backend.Integration.IntegrationTest
import backend.Integration.getTokens
import backend.Integration.toJsonString
import backend.model.event.Team
import backend.model.misc.Coord
import backend.model.user.Admin
import backend.model.user.Participant
import org.junit.Before
import org.junit.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime

open class InvoiceControllerTest : IntegrationTest() {

    private lateinit var tokens: Pair<String, String>
    private lateinit var team: Team

    @Before
    override fun setUp() {
        super.setUp()
        val creator = userService.create("creator@mail.de", "password", { addRole(Participant::class) })
        val invitee = userService.create("invitee@mail.de", "password", { addRole(Participant::class) })
        userService.create("admin@mail.de", "password", { addRole(Admin::class) })
        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 1.1), 36)
        team = teamService.create(creator.getRole(Participant::class)!!, "name", "description", event)
        team.members.add(invitee.getRole(Participant::class)!!)
        invitee.getRole(Participant::class)!!.currentTeam = team
        teamService.save(team)
        userService.save(invitee)
        tokens = getTokens(this.mockMvc, "admin@mail.de", "password")
    }

    @Test
    open fun testCreatePayment() {

        val content = mapOf(
                "amount" to 30.0
        ).toJsonString()

        val request = MockMvcRequestBuilders.post("/invoice/${team.invoice!!.id}/payment/")
                .header("Authorization", "Bearer ${tokens.first}")
                .contentType(APPLICATION_JSON_UTF_8)
                .content(content)

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.amount").value(60.0))
                .andExpect(jsonPath("$.team").value(team.id!!.toInt()))
                .andExpect(jsonPath("$.payments").isArray)
                .andExpect(jsonPath("$.payments.[0]").exists())
                .andExpect(jsonPath("$.payments.[1]").doesNotExist())
    }
}
