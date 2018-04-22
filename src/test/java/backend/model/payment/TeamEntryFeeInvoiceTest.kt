package backend.model.payment

import backend.model.event.Team
import backend.model.user.Admin
import backend.util.euroOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
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
        val invoice = TeamEntryFeeInvoice(team, euroOf(10))
        assertEquals(invoice.team, team)
    }

    @Test
    fun testIsPaymentEligable() {
        val invoice = TeamEntryFeeInvoice(team, euroOf(10))
        val payment = AdminPayment(euroOf(10), admin)

        PowerMockito.`when`(team.isFull()).thenReturn(true)
        // Test passes if this does not throw
        invoice.checkPaymentEligability(payment)
    }

    @Test
    fun testGetAmount() {
        val invoice = TeamEntryFeeInvoice(team, euroOf(10))
        assertEquals(invoice.amount, euroOf(10))
    }

    @Test
    fun testAddPayment() {
        val invoice = TeamEntryFeeInvoice(team, euroOf(10))
        val payment = AdminPayment(euroOf(10), admin)

        PowerMockito.`when`(team.isFull()).thenReturn(true)

        invoice.addPayment(payment)
    }

    @Test
    fun testAddPaymentTwoTimesHalfAmountFailsBecauseOnlyFullAmountCanBeAdded() {
        val invoice = TeamEntryFeeInvoice(team, euroOf(10))
        val firstPayment = AdminPayment(euroOf(5), admin)
        val secondPayment = AdminPayment(euroOf(5), admin)

        PowerMockito.`when`(team.isFull()).thenReturn(true)

        assertFails {
            invoice.addPayment(firstPayment)
            invoice.addPayment(secondPayment)
        }
    }

    @Test
    fun testAddPaymentFullAmount() {
        val invoice = TeamEntryFeeInvoice(team, euroOf(10))
        val payment = AdminPayment(euroOf(10), admin)

        PowerMockito.`when`(team.isFull()).thenReturn(true)

        invoice.addPayment(payment)
    }

    fun testFailToAddTooManyPayments() {
        val invoice = TeamEntryFeeInvoice(team, euroOf(10))
        val firstPayment = AdminPayment(euroOf(5), admin)
        val secondPayment = AdminPayment(euroOf(5), admin)
        val thirdPayment = AdminPayment(euroOf(5), admin)

        PowerMockito.`when`(team.isFull()).thenReturn(true)

        invoice.addPayment(firstPayment)
        invoice.addPayment(secondPayment)
        assertFails { invoice.addPayment(thirdPayment) }
    }

    @Test
    fun testFailToAddPaymentIfAmountIsWrong() {
        val invoice = TeamEntryFeeInvoice(team, euroOf(10))
        val payment = AdminPayment(euroOf(4), admin)

        assertFails { invoice.addPayment(payment) }
    }

    @Test
    fun testFailToAddPaymentIfTeamHasOnlyOneMember() {
        val invoice = TeamEntryFeeInvoice(team, euroOf(10))
        val payment = AdminPayment(euroOf(5), admin)

        PowerMockito.`when`(team.isFull()).thenReturn(false)

        assertFails { invoice.addPayment(payment) }
    }

    @Test
    fun testIsFullyPaid() {
        val invoice = TeamEntryFeeInvoice(team, euroOf(10))

        val firstPayment = AdminPayment(euroOf(10), admin)

        PowerMockito.`when`(team.isFull()).thenReturn(true)

        invoice.addPayment(firstPayment)

        assertTrue(invoice.isFullyPaid())
    }

    @Test
    fun testAmountOfCurrentPayments() {
        val invoice = TeamEntryFeeInvoice(team, euroOf(10))
        val firstPayment = AdminPayment(euroOf(10), admin)

        PowerMockito.`when`(team.isFull()).thenReturn(true)

        invoice.addPayment(firstPayment)

        assertEquals(euroOf(10), invoice.amountOfCurrentPayments())
    }

    @Test
    fun testGetPayments() {
        val invoice = TeamEntryFeeInvoice(team, euroOf(10))
        val firstPayment = AdminPayment(euroOf(10), admin)

        PowerMockito.`when`(team.isFull()).thenReturn(true)

        invoice.addPayment(firstPayment)

        val payments = invoice.getPayments()

        assertEquals(1, payments.count())
    }
}
