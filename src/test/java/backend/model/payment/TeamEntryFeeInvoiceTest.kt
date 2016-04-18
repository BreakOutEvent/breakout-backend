package backend.model.payment

import backend.model.event.Team
import backend.model.user.Participant
import backend.model.user.User
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
@PrepareForTest(User::class, Team::class, Payment::class, Participant::class)
class TeamEntryFeeInvoiceTest {

    private lateinit var team: Team
    private lateinit var payment: Payment
    private lateinit var user: User
    private lateinit var participant: Participant

    @Before
    fun setUp() {
        team = PowerMockito.mock(Team::class.java)
        payment = PowerMockito.mock(Payment::class.java)
        user = PowerMockito.mock(User::class.java)
        participant = PowerMockito.mock(Participant::class.java)
    }

    @Test
    fun testGetTeam() {
        val invoice = TeamEntryFeeInvoice(team, Money.of(BigDecimal.TEN, "EUR"))

        assertEquals(invoice.team, team)
    }

    @Test
    fun testIsPaymentEligable() {
        val invoice = TeamEntryFeeInvoice(team, Money.of(BigDecimal.TEN, "EUR"))

        PowerMockito.`when`(payment.user()).thenReturn(user)
        PowerMockito.`when`(user.getRole(Participant::class)).thenReturn(participant)
        PowerMockito.`when`(payment.amount).thenReturn(Money.of(BigDecimal.valueOf(5), "EUR"))
        PowerMockito.`when`(team.isMember(participant)).thenReturn(true)
        PowerMockito.`when`(team.isFull()).thenReturn(true)

        assertTrue(invoice.isPaymentEligable(payment))
    }

    @Test
    fun testGetAmount() {
        val invoice = TeamEntryFeeInvoice(team, Money.of(BigDecimal.TEN, "EUR"))

        assertEquals(invoice.amount, Money.of(BigDecimal.TEN, "EUR"));
    }

    @Test
    fun testAddPayment() {
        val invoice = TeamEntryFeeInvoice(team, Money.of(BigDecimal.TEN, "EUR"))

        PowerMockito.`when`(payment.user()).thenReturn(user)
        PowerMockito.`when`(user.getRole(Participant::class)).thenReturn(participant)
        PowerMockito.`when`(payment.amount).thenReturn(Money.of(BigDecimal.valueOf(5.0), "EUR"))
        PowerMockito.`when`(team.isMember(participant)).thenReturn(true)
        PowerMockito.`when`(team.isFull()).thenReturn(true)

        invoice.addPayment(payment)
    }

    @Test
    fun testFailToAddPaymentIfAmountIsWrong() {
        val invoice = TeamEntryFeeInvoice(team, Money.of(BigDecimal.TEN, "EUR"))

        PowerMockito.`when`(payment.user()).thenReturn(user)
        PowerMockito.`when`(user.getRole(Participant::class)).thenReturn(participant)
        PowerMockito.`when`(payment.amount).thenReturn(Money.of(BigDecimal.valueOf(4.0), "EUR"))
        PowerMockito.`when`(team.isMember(participant)).thenReturn(true)

        assertFails { invoice.addPayment(payment) }
    }

    @Test
    fun testFailToAddPaymentIfTeamHasOnlyOneMember() {
        val invoice = TeamEntryFeeInvoice(team, Money.of(BigDecimal.TEN, "EUR"))

        PowerMockito.`when`(payment.user()).thenReturn(user)
        PowerMockito.`when`(user.getRole(Participant::class)).thenReturn(participant)
        PowerMockito.`when`(payment.amount).thenReturn(Money.of(BigDecimal.valueOf(4.0), "EUR"))
        PowerMockito.`when`(team.isMember(participant)).thenReturn(true)
        PowerMockito.`when`(team.isFull()).thenReturn(false)

        assertFails { invoice.addPayment(payment) }
    }

    @Test
    fun testFailToAddPaymentIfUserIsNotPartOfTeam() {
        val invoice = TeamEntryFeeInvoice(team, Money.of(BigDecimal.TEN, "EUR"))

        PowerMockito.`when`(payment.user()).thenReturn(user)
        PowerMockito.`when`(user.getRole(Participant::class)).thenReturn(participant)
        PowerMockito.`when`(payment.amount).thenReturn(Money.of(BigDecimal.valueOf(5.0), "EUR"))
        PowerMockito.`when`(team.isMember(participant)).thenReturn(false) // This is the important line!

        assertFails { invoice.addPayment(payment) }
    }

    @Test
    fun testIsFullyPaid() {
        val invoice = TeamEntryFeeInvoice(team, Money.of(BigDecimal.TEN, "EUR"))

        PowerMockito.`when`(payment.user()).thenReturn(user)
        PowerMockito.`when`(user.getRole(Participant::class)).thenReturn(participant)
        PowerMockito.`when`(payment.amount).thenReturn(Money.of(BigDecimal.valueOf(5.0), "EUR"))
        PowerMockito.`when`(team.isMember(participant)).thenReturn(true)
        PowerMockito.`when`(team.isFull()).thenReturn(true)

        val secondPayment = PowerMockito.mock(Payment::class.java)
        val secondTeam = PowerMockito.mock(Team::class.java)
        val secondUser = PowerMockito.mock(User::class.java)
        val secondParticipant = PowerMockito.mock(Participant::class.java)

        PowerMockito.`when`(secondPayment.user()).thenReturn(secondUser)
        PowerMockito.`when`(secondUser.getRole(Participant::class)).thenReturn(participant)
        PowerMockito.`when`(secondPayment.amount).thenReturn(Money.of(BigDecimal.valueOf(5.0), "EUR"))
        PowerMockito.`when`(secondTeam.isMember(secondParticipant)).thenReturn(true)
        PowerMockito.`when`(secondTeam.isFull()).thenReturn(true)

        invoice.addPayment(payment)
        invoice.addPayment(secondPayment)

        assertTrue(invoice.isFullyPaid())
    }

    @Test
    fun testAmountOfCurrentPayments() {
        val invoice = TeamEntryFeeInvoice(team, Money.of(BigDecimal.TEN, "EUR"))

        PowerMockito.`when`(payment.user()).thenReturn(user)
        PowerMockito.`when`(user.getRole(Participant::class)).thenReturn(participant)
        PowerMockito.`when`(payment.amount).thenReturn(Money.of(BigDecimal.valueOf(5.0), "EUR"))
        PowerMockito.`when`(team.isMember(participant)).thenReturn(true)
        PowerMockito.`when`(team.isFull()).thenReturn(true)

        val secondPayment = PowerMockito.mock(Payment::class.java)
        val secondTeam = PowerMockito.mock(Team::class.java)
        val secondUser = PowerMockito.mock(User::class.java)
        val secondParticipant = PowerMockito.mock(Participant::class.java)

        PowerMockito.`when`(secondPayment.user()).thenReturn(secondUser)
        PowerMockito.`when`(secondUser.getRole(Participant::class)).thenReturn(participant)
        PowerMockito.`when`(secondPayment.amount).thenReturn(Money.of(BigDecimal.valueOf(5.0), "EUR"))
        PowerMockito.`when`(secondTeam.isMember(secondParticipant)).thenReturn(true)
        PowerMockito.`when`(secondTeam.isFull()).thenReturn(true)

        invoice.addPayment(payment)
        invoice.addPayment(secondPayment)

        assertEquals(Money.of(BigDecimal.TEN, "EUR"), invoice.amountOfCurrentPayments())
    }

    @Test
    fun testGetPayments() {
        val invoice = TeamEntryFeeInvoice(team, Money.of(BigDecimal.TEN, "EUR"))

        PowerMockito.`when`(payment.user()).thenReturn(user)
        PowerMockito.`when`(user.getRole(Participant::class)).thenReturn(participant)
        PowerMockito.`when`(payment.amount).thenReturn(Money.of(BigDecimal.valueOf(5.0), "EUR"))
        PowerMockito.`when`(team.isMember(participant)).thenReturn(true)
        PowerMockito.`when`(team.isFull()).thenReturn(true)

        val secondPayment = PowerMockito.mock(Payment::class.java)
        val secondTeam = PowerMockito.mock(Team::class.java)
        val secondUser = PowerMockito.mock(User::class.java)
        val secondParticipant = PowerMockito.mock(Participant::class.java)

        PowerMockito.`when`(secondPayment.user()).thenReturn(secondUser)
        PowerMockito.`when`(secondUser.getRole(Participant::class)).thenReturn(participant)
        PowerMockito.`when`(secondPayment.amount).thenReturn(Money.of(BigDecimal.valueOf(5.0), "EUR"))
        PowerMockito.`when`(secondTeam.isMember(secondParticipant)).thenReturn(true)
        PowerMockito.`when`(secondTeam.isFull()).thenReturn(true)

        invoice.addPayment(payment)
        invoice.addPayment(secondPayment)

        val payments = invoice.getPayments()

        assertEquals(2, payments.count())
    }
}
