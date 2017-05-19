package backend.model.payment

import backend.model.challenges.Challenge
import backend.model.challenges.ChallengeStatus
import backend.model.sponsoring.Sponsoring
import backend.model.sponsoring.UnregisteredSponsor
import backend.model.user.Sponsor
import backend.util.euroOf
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito
import org.powermock.api.mockito.PowerMockito.`when`
import org.powermock.api.mockito.PowerMockito.mock
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(PowerMockRunner::class)
@PrepareForTest(Challenge::class, Sponsoring::class, Sponsor::class, UnregisteredSponsor::class)
class SponsoringInvoiceTest {

    @Test
    fun getSponsorings() {

        // given a sponsor with 3 sponsorings
        val sponsoring1 = mock(Sponsoring::class.java)
        val sponsoring2 = mock(Sponsoring::class.java)
        val sponsoring3 = mock(Sponsoring::class.java)
        val sponsorings = mutableListOf(sponsoring1, sponsoring2, sponsoring3)
        val sponsor = mock(Sponsor::class.java)

        `when`(sponsor.sponsorings).thenReturn(sponsorings)

        // when creating the invoice for this sponsor
        val invoice = SponsoringInvoice(sponsor)

        // then it has exactly those 3 sponsorings
        assertEquals(sponsorings, invoice.sponsorings)

    }

    @Test
    fun getChallenges() {

        // given a sponsor with 2 challenges
        val challenge1 = mock(Challenge::class.java)
        val challenge2 = mock(Challenge::class.java)
        val challenges = mutableListOf(challenge1, challenge2)
        val sponsor = mock(Sponsor::class.java)

        `when`(sponsor.challenges).thenReturn(challenges)

        // when creating this invoice
        val invoice = SponsoringInvoice(sponsor)

        // then it contains exactly those two challenges
        assertEquals(challenges, invoice.challenges)
    }

    @Test
    fun getSponsorRegistered() {
        val sponsor = mock(Sponsor::class.java)
        val invoice = SponsoringInvoice(sponsor)
        assertTrue(invoice.sponsor is Sponsor)
    }

    @Test
    fun getSponsorUnregistered() {
        val sponsor = mock(UnregisteredSponsor::class.java)
        val invoice = SponsoringInvoice(sponsor)
        assertTrue(invoice.sponsor is UnregisteredSponsor)
    }

    @Test
    fun checkPaymentEligability() {
    }

    @Test
    fun generatePurposeOfTransfer() {
    }

    @Test
    fun toEmailOverview() {
        val sponsor = PowerMockito.mock(Sponsor::class.java)

        val firstChallenge = mock(Challenge::class.java)
        val secondChallenge = mock(Challenge::class.java)

        val firstSponsoring = mock(Sponsoring::class.java)
        val secondSponsoring = mock(Sponsoring::class.java)

        `when`(sponsor.challenges).thenReturn(mutableListOf(firstChallenge, secondChallenge))
        `when`(sponsor.sponsorings).thenReturn(mutableListOf(firstSponsoring, secondSponsoring))

        `when`(firstChallenge.amount).thenReturn(euroOf(10))
        `when`(firstChallenge.description).thenReturn("a potentially very long text a potentially very long texta potentially very long texta potentially very long texta potentially very long texta potentially very long texta potentially very long texta potentially very long texta potentially very long texta potentially very long texta potentially very long text")
        `when`(firstChallenge.status).thenReturn(ChallengeStatus.WITH_PROOF)
        `when`(firstChallenge.billableAmount()).thenReturn(euroOf(10))

        `when`(secondChallenge.amount).thenReturn(euroOf(80))
        `when`(secondChallenge.status).thenReturn(ChallengeStatus.WITH_PROOF)
        `when`(secondChallenge.description).thenReturn("Take a photo")
        `when`(secondChallenge.billableAmount()).thenReturn(euroOf(80))

        `when`(firstSponsoring.amountPerKm).thenReturn(euroOf(2.0))
        `when`(firstSponsoring.limit).thenReturn(euroOf(100))
        `when`(firstSponsoring.billableAmount()).thenReturn(euroOf(33.90))


        `when`(secondSponsoring.amountPerKm).thenReturn(euroOf(0.01))
        `when`(secondSponsoring.limit).thenReturn(euroOf(100))
        `when`(secondSponsoring.billableAmount()).thenReturn(euroOf(28.70))

        val invoice = SponsoringInvoice(sponsor)

        assertEquals("""
        <b>Challenges</b><br/>
        <b>Team-ID</b> null <b>Teamname</b> null <b>Description</b> a potentially very long text a potentially very lo... <b>Status</b> WITH_PROOF <b>Amount</b> EUR 10 <b>Billed Amount</b> EUR 10<br/>
        <b>Team-ID</b> null <b>Teamname</b> null <b>Description</b> Take a photo... <b>Status</b> WITH_PROOF <b>Amount</b> EUR 80 <b>Billed Amount</b> EUR 80<br/>

        <br/><b>Sponsorings</b><br/>
        <b>Team-ID</b> null <b>Teamname</b> null <b>Status</b> null <b>Amount Per Km</b> EUR 2 <b>Actual Km</b> null <b>Billed Amount</b> EUR 33.9<br/>
        <b>Team-ID</b> null <b>Teamname</b> null <b>Status</b> null <b>Amount Per Km</b> EUR 0.01 <b>Actual Km</b> null <b>Billed Amount</b> EUR 28.7<br/>
        <br/>

        <b>Total:</b> EUR 152.6<br/>
        <b>Already paid:</b> EUR 0<br/>""".trimIndent(), invoice.toEmailOverview())
    }

}
