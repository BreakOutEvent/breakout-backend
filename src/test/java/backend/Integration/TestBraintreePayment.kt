package backend.Integration

import backend.model.event.Event
import backend.model.event.Team
import backend.model.misc.Coord
import backend.model.payment.Invoice
import backend.model.payment.TeamEntryFeeInvoice
import backend.model.user.Participant
import backend.model.user.User
import org.javamoney.moneta.Money
import org.junit.Before
import org.junit.Test
import org.springframework.http.HttpMethod.GET
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.time.LocalDateTime

class TestBraintreePayment : IntegrationTest() {

    private lateinit var invoice: TeamEntryFeeInvoice
    private lateinit var event: Event
    private lateinit var team: Team
    private lateinit var user: User
    private lateinit var tokens: Pair<String, String>

    @Before
    override fun setUp() {
        super.setUp()
        user = userService.create("email@mail.com", "password", { addRole(Participant::class); isBlocked = false })
        event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 0.0), 36)
        team = teamService.create(user.getRole(Participant::class)!!, "name", "description", event)
        invoice = TeamEntryFeeInvoice(team, Money.of(BigDecimal.valueOf(60.0), "EUR"))
        invoiceService.save(invoice)
        tokens = getTokens(this.mockMvc, "email@mail.com", "password")
    }

    @Test
    fun testGetTokenForBraintreePaymentForInvoice() {

        val request = MockMvcRequestBuilders
                .request(GET, "/invoice/${invoice.id}/payment/braintree/client_token/")
                .header("Authorization", "Bearer ${tokens.first}")
                .contentType(APPLICATION_JSON_VALUE)

        val response = mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.token").exists())
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    fun testFailToGetTokenForNonexistingPayment() {
        val request = MockMvcRequestBuilders
                .request(GET, "/invoice/321/payment/braintree/client_token/")
                .header("Authorization", "Bearer ${tokens.first}")
                .contentType(APPLICATION_JSON_VALUE)

        mockMvc.perform(request)
                .andExpect(status().isNotFound)
    }

    @Test
    fun testFailToGetTokenIfUnauthenticated() {
        val request = MockMvcRequestBuilders
                .request(GET, "/invoice/1/payment/braintree/client_token/")
                .contentType(APPLICATION_JSON_VALUE)

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized)
    }

}
