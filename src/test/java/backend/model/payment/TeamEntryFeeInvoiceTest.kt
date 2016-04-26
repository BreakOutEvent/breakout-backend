package backend.model.payment

import backend.model.event.Team
import backend.model.user.Admin
import org.javamoney.moneta.Money
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

@RunWith(PowerMockRunner::class)
@PrepareForTest(Team::class, Admin::class)
class TeamEntryFeeInvoiceTest {

    private lateinit var team: Team
    private lateinit var admin: Admin

    @Before
    fun setUp() {
        team = PowerMockito.mock(Team::class.java)
        admin = PowerMockito.mock(Admin::class.java)
    }

    @Test
    fun testGetTeam() {
        val invoice = TeamEntryFeeInvoice(team, Money.of(BigDecimal.TEN, "EUR"))
        assertEquals(invoice.team, team)
    }

    @Test
    fun testIsPaymentEligable() {
        val invoice = TeamEntryFeeInvoice(team, Money.of(BigDecimal.TEN, "EUR"))
        val payment = AdminPayment(Money.of(BigDecimal.valueOf(5), "EUR"), admin)

        PowerMockito.`when`(team.isFull()).thenReturn(true)
        // Test passes if this does not throw
        invoice.checkPaymentEligability(payment)
    }

    @Test
    fun testGetAmount() {
        val invoice = TeamEntryFeeInvoice(team, Money.of(BigDecimal.TEN, "EUR"))
        assertEquals(invoice.amount, Money.of(BigDecimal.TEN, "EUR"));
    }

    @Test
    fun testAddPayment() {
        val invoice = TeamEntryFeeInvoice(team, Money.of(BigDecimal.TEN, "EUR"))
        val payment = AdminPayment(Money.of(BigDecimal.valueOf(5), "EUR"), admin)

        PowerMockito.`when`(team.isFull()).thenReturn(true)

        invoice.addPayment(payment)
    }

    @Test
    fun testAddPaymentTwoTimesHalfAmount() {
        val invoice = TeamEntryFeeInvoice(team, Money.of(BigDecimal.TEN, "EUR"))
        val firstPayment = AdminPayment(Money.of(BigDecimal.valueOf(5), "EUR"), admin)
        val secondPayment = AdminPayment(Money.of(BigDecimal.valueOf(5), "EUR"), admin)

        PowerMockito.`when`(team.isFull()).thenReturn(true)

        invoice.addPayment(firstPayment)
        invoice.addPayment(secondPayment)
    }

    @Test
    fun testAddPaymentFullAmount() {
        val invoice = TeamEntryFeeInvoice(team, Money.of(BigDecimal.TEN, "EUR"))
        val payment = AdminPayment(Money.of(BigDecimal.valueOf(10), "EUR"), admin)

        PowerMockito.`when`(team.isFull()).thenReturn(true)

        invoice.addPayment(payment)
    }

    fun testFailToAddTooManyPayments() {
        val invoice = TeamEntryFeeInvoice(team, Money.of(BigDecimal.TEN, "EUR"))
        val firstPayment = AdminPayment(Money.of(BigDecimal.valueOf(5), "EUR"), admin)
        val secondPayment = AdminPayment(Money.of(BigDecimal.valueOf(5), "EUR"), admin)
        val thirdPayment = AdminPayment(Money.of(BigDecimal.valueOf(5), "EUR"), admin)

        PowerMockito.`when`(team.isFull()).thenReturn(true)

        invoice.addPayment(firstPayment)
        invoice.addPayment(secondPayment)
        assertFails { invoice.addPayment(thirdPayment) }
    }

    @Test
    fun testFailToAddPaymentIfAmountIsWrong() {
        val invoice = TeamEntryFeeInvoice(team, Money.of(BigDecimal.TEN, "EUR"))
        val payment = AdminPayment(Money.of(BigDecimal.valueOf(4), "EUR"), admin)

        assertFails { invoice.addPayment(payment) }
    }

    @Test
    fun testFailToAddPaymentIfTeamHasOnlyOneMember() {
        val invoice = TeamEntryFeeInvoice(team, Money.of(BigDecimal.TEN, "EUR"))
        val payment = AdminPayment(Money.of(BigDecimal.valueOf(5), "EUR"), admin)

        PowerMockito.`when`(team.isFull()).thenReturn(false)

        assertFails { invoice.addPayment(payment) }
    }

    @Test
    fun testIsFullyPaid() {
        val invoice = TeamEntryFeeInvoice(team, Money.of(BigDecimal.TEN, "EUR"))

        val firstPayment = AdminPayment(Money.of(BigDecimal.valueOf(5), "EUR"), admin)
        val secondPayment = AdminPayment(Money.of(BigDecimal.valueOf(5), "EUR"), admin)

        PowerMockito.`when`(team.isFull()).thenReturn(true)

        invoice.addPayment(firstPayment)
        invoice.addPayment(secondPayment)

        assertTrue(invoice.isFullyPaid())
    }

    @Test
    fun testAmountOfCurrentPayments() {
        val invoice = TeamEntryFeeInvoice(team, Money.of(BigDecimal.TEN, "EUR"))
        val firstPayment = AdminPayment(Money.of(BigDecimal.valueOf(5), "EUR"), admin)
        val secondPayment = AdminPayment(Money.of(BigDecimal.valueOf(5), "EUR"), admin)

        PowerMockito.`when`(team.isFull()).thenReturn(true)

        invoice.addPayment(firstPayment)
        invoice.addPayment(secondPayment)

        assertEquals(Money.of(BigDecimal.TEN, "EUR"), invoice.amountOfCurrentPayments())
    }

    @Test
    fun testGetPayments() {
        val invoice = TeamEntryFeeInvoice(team, Money.of(BigDecimal.TEN, "EUR"))
        val firstPayment = AdminPayment(Money.of(BigDecimal.valueOf(5), "EUR"), admin)
        val secondPayment = AdminPayment(Money.of(BigDecimal.valueOf(5), "EUR"), admin)

        PowerMockito.`when`(team.isFull()).thenReturn(true)

        invoice.addPayment(firstPayment)
        invoice.addPayment(secondPayment)

        val payments = invoice.getPayments()

        assertEquals(2, payments.count())
    }
}
