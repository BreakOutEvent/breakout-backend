package backend.controller

import backend.Integration.IntegrationTest
import backend.model.event.Team
import backend.model.misc.Coord
import backend.model.payment.SponsoringInvoice
import backend.model.user.Admin
import backend.model.user.Participant
import backend.model.user.User
import backend.testHelper.asUser
import backend.testHelper.json
import org.javamoney.moneta.Money
import org.junit.Before
import org.junit.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

open class InvoiceControllerTest : IntegrationTest() {

    private lateinit var team: Team
    private lateinit var admin: Admin
    private lateinit var sponsoringInvoice: SponsoringInvoice
    private lateinit var creator: User

    @Before
    override fun setUp() {
        super.setUp()
        creator = userService.create("creator@mail.de", "password", { addRole(Participant::class) })
        val invitee = userService.create("invitee@mail.de", "password", { addRole(Participant::class) })
        admin = userService.create("admin@mail.de", "password", { addRole(Admin::class) }).getRole(Admin::class)!!
        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 1.1), 36)
        team = teamService.create(creator.getRole(Participant::class)!!, "name", "description", event)
        team.members.add(invitee.getRole(Participant::class)!!)
        invitee.getRole(Participant::class)!!.setCurrentTeam(team)
        teamService.save(team)
        userService.save(invitee)

        sponsoringInvoice = sponsoringInvoiceService.createInvoice(team, Money.of(20, "EUR"), "", "test", "test2")
    }

    @Test
    fun testGetInvoiceAndPaymentsForTeamSponsoring() {

        val requestTeamMember = get("/invoice/sponsoring/${team.id}/")
                .asUser(mockMvc, creator.email, "password")

        mockMvc.perform(requestTeamMember)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].amount").value(20.0))
                .andExpect(jsonPath("$[0].teamId").value(team.id!!.toInt()))
                .andExpect(jsonPath("$[0].firstname").value("test"))
                .andExpect(jsonPath("$[0].lastname").value("test2"))
                .andExpect(jsonPath("$[0].company").value(""))
                .andExpect(jsonPath("$[0].payments").isArray)

        val requestAdmin = get("/invoice/sponsoring/${team.id}/")
                .asUser(mockMvc, admin.email, "password")

        mockMvc.perform(requestAdmin).andExpect(status().isOk)

        val requestUnauth = get("/invoice/sponsoring/${team.id}/")
        mockMvc.perform(requestUnauth).andExpect(status().isUnauthorized)

    }


    @Test
    open fun testCreatePayment() {

        val body = mapOf("amount" to 30.0)

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

    @Test
    open fun testCreatePaymentForSponsoringInvoice() {

        val body = mapOf("amount" to 30.0)

        val request = post("/invoice/${sponsoringInvoice.id}/payment/")
                .asUser(mockMvc, admin.email, "password")
                .json(body)

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.amount").value(20.0))
                .andExpect(jsonPath("$.teamId").value(team.id!!.toInt()))
                .andExpect(jsonPath("$.payments").isArray)
                .andExpect(jsonPath("$.payments.[0].amount").value(30.0))
                .andExpect(jsonPath("$.payments.[0]").exists())
                .andExpect(jsonPath("$.payments.[1]").doesNotExist())
    }

    @Test
    open fun testCreateInvoice() {
        val body = mapOf(
                "teamId" to team.id,
                "amount" to 30.0,
                "firstname" to "test",
                "lastname" to "test2",
                "company" to ""
        )

        val request = post("/invoice/sponsoring/")
                .asUser(mockMvc, admin.email, "password")
                .json(body)

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.amount").value(30.0))
                .andExpect(jsonPath("$.teamId").value(team.id!!.toInt()))
                .andExpect(jsonPath("$.firstname").value("test"))
                .andExpect(jsonPath("$.lastname").value("test2"))
                .andExpect(jsonPath("$.company").value(""))

        val body2 = mapOf(
                "teamId" to team.id,
                "amount" to 30,
                "firstname" to "test",
                "lastname" to "test2",
                "company" to ""
        )

        val request2 = post("/invoice/sponsoring/")
                .asUser(mockMvc, admin.email, "password")
                .json(body2)

        mockMvc.perform(request2)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.amount").value(30.0))
                .andExpect(jsonPath("$.teamId").value(team.id!!.toInt()))
                .andExpect(jsonPath("$.firstname").value("test"))
                .andExpect(jsonPath("$.lastname").value("test2"))
                .andExpect(jsonPath("$.company").value(""))
    }
}
