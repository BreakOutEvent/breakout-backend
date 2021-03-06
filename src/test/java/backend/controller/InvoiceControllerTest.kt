package backend.controller

import backend.Integration.IntegrationTest
import backend.model.event.Team
import backend.model.misc.Coord
import backend.model.user.Admin
import backend.model.user.Participant
import backend.model.user.User
import backend.testHelper.asUser
import backend.testHelper.json
import org.junit.Before
import org.junit.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

// Currently only tests TeamEntryFeeInvoices, not SponsoringInvoices!
open class InvoiceControllerTest : IntegrationTest() {

    private lateinit var team: Team
    private lateinit var admin: Admin
    private lateinit var creator: User

    @Before
    override fun setUp() {
        super.setUp()
        creator = userService.create("creator@mail.de", "password", { addRole(Participant::class) })
        val invitee = userService.create("invitee@mail.de", "password", { addRole(Participant::class) })
        admin = userService.create("admin@mail.de", "password", { addRole(Admin::class) }).getRole(Admin::class)!!
        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 1.1), 36)
        team = teamService.create(creator.getRole(Participant::class)!!, "name", "description", event, null)
        team.members.add(invitee.getRole(Participant::class)!!)
        invitee.getRole(Participant::class)!!.setCurrentTeam(team)
        teamService.save(team)
        userService.save(invitee)
    }

    @Test
    open fun testCreatePayment() {

        val body = mapOf("amount" to 60.0)

        val request = post("/invoice/${team.invoice!!.id}/payment/")
                .asUser(mockMvc, admin.email, "password")
                .json(body)

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.amount").value(60.0))
                .andExpect(jsonPath("$.team").value(team.id!!.toInt()))
                .andExpect(jsonPath("$.payments").isArray)
                .andExpect(jsonPath("$.payments.[0]").exists())
                .andExpect(jsonPath("$.payments.[1]").doesNotExist())
    }


    @Test
    open fun testGetInvoice() {
        val request = get("/invoice/teamfee/${team.invoice!!.id}/")
                .asUser(mockMvc, admin.email, "password")

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.amount").value(60.0))
                .andExpect(jsonPath("$.team").value(team.id!!.toInt()))
                .andExpect(jsonPath("$.payments").isArray)
    }
}
