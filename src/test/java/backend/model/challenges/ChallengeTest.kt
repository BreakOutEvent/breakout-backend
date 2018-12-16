package backend.model.challenges

import backend.model.challenges.ChallengeStatus.*
import backend.model.event.Team
import backend.model.posting.Posting
import backend.model.sponsoring.UnregisteredSponsor
import backend.model.user.Sponsor
import backend.util.euroOf
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito.`when`
import org.powermock.api.mockito.PowerMockito.mock
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(PowerMockRunner::class)
@PrepareForTest(Sponsor::class, Team::class, Posting::class, UnregisteredSponsor::class)
class ChallengeTest {

    @Test
    fun testSetSponsor() {
        val sponsor = mock(Sponsor::class.java)
        val team = mock(Team::class.java)

        val challenge = Challenge(sponsor, team, euroOf(50), "description")

        assertEquals(sponsor, challenge.sponsor)
        assertNull(challenge.sponsor.unregisteredSponsor)
    }

    @Test
    fun testSetUnregisteredSponsor() {
        val unregistered = mock(UnregisteredSponsor::class.java)
        val team = mock(Team::class.java)

        val challenge = Challenge(unregistered, team, euroOf(50), "description")

        assertTrue(challenge.sponsor is UnregisteredSponsor)
        assertNull(challenge.sponsor.registeredSponsor)
    }

    @Test
    fun testReject() {
        val sponsor = mock(Sponsor::class.java)
        val team = mock(Team::class.java)

        val challenge = Challenge(sponsor, team, euroOf(50), "description")

        assertEquals(PROPOSED, challenge.status)
        challenge.reject()
        assertEquals(REJECTED, challenge.status)
    }

    @Test
    fun testAddProof() {
        val sponsor = mock(Sponsor::class.java)
        val team = mock(Team::class.java)
        val proof = mock(Posting::class.java)

        val challenge = Challenge(sponsor, team, euroOf(50), "description")
        challenge.maximumCount = 2

        challenge.addProof()
        challenge.addProof()
        assertEquals(WITH_PROOF, challenge.status)
        assertEquals(2, challenge.fulfilledCount)

        assertFails { challenge.addProof() }
    }

    @Test
    fun testTakeBack() {
        val sponsor = mock(Sponsor::class.java)
        val team = mock(Team::class.java)
        val proof = mock(Posting::class.java)

        val challenge = Challenge(sponsor, team, euroOf(50), "description")
        challenge.addProof()
        challenge.takeBack()

        assertEquals(PROPOSED, challenge.status)
        assertEquals(0, challenge.fulfilledCount)
    }

    @Test
    fun testWithdraw() {
        val team = mock(Team::class.java)
        val sponsor = mock(Sponsor::class.java)
        val challenge = Challenge(sponsor, team, euroOf(50), "description")

        challenge.withdraw()

        assertEquals(WITHDRAWN, challenge.status)
        assertFails { challenge.reject() }
    }

    @Test
    fun whenSponsorIsRegistered_thenGetSponsorReturnsSponsor() {
        val sponsor = mock(Sponsor::class.java)
        val team = mock(Team::class.java)
        val challenge = Challenge(sponsor, team, euroOf(50), "description")

        assertTrue { challenge.sponsor is Sponsor }
    }

    @Test
    fun whenSponsorIsUnregistered_thenGetSponsorReturnsUnregisteredSponsor() {
        val sponsor = mock(UnregisteredSponsor::class.java)
        val team = mock(Team::class.java)
        val challenge = Challenge(sponsor, team, euroOf(50), "description")

        assertTrue { challenge.sponsor is UnregisteredSponsor }
    }

    fun whenSponsorIsUnregistered_thenHasRegisteredSponsorReturnsFalse() {
        val sponsor = mock(Sponsor::class.java)
        val team = mock(Team::class.java)
        val challenge = Challenge(sponsor, team, euroOf(50), "description")

        assertFalse { challenge.hasRegisteredSponsor() }
    }

    fun whenSponsorIsRegistered_thenHasRegisteredSponsorReturnsTrue() {
        val sponsor = mock(Sponsor::class.java)
        val team = mock(Team::class.java)
        val challenge = Challenge(sponsor, team, euroOf(50), "description")

        assertTrue { challenge.hasRegisteredSponsor() }
    }

    @Test
    fun whenChallengeIsFullfilled_thenBillableAmountReturnsAmountOfChallenge() {
        val challenge = Challenge(mock(Sponsor::class.java), mock(Team::class.java), euroOf(50), "description")

        challenge.addProof()

        assertEquals(euroOf(50), challenge.billableAmount())
    }

    @Test
    fun whenChallengeIsFullfilledMultipleTimes_thenBillableAmountReturnsAmountOfChallenge() {
        val challenge = Challenge(mock(Sponsor::class.java), mock(Team::class.java), euroOf(50), "description")
        challenge.maximumCount = null

        challenge.addProof()
        challenge.addProof()

        assertEquals(euroOf(100), challenge.billableAmount())
    }

    @Test
    fun whenChallengeIsProposed_thenBillableAmountIsZero() {
        val challenge = Challenge(mock(Sponsor::class.java), mock(Team::class.java), euroOf(50), "description")

        assertEquals(euroOf(0.0), challenge.billableAmount())
    }

    @Test
    fun whenChallengeIsWithdrawn_thenBillableAmountIsZero() {
        val challenge = Challenge(mock(Sponsor::class.java), mock(Team::class.java), euroOf(50), "description")

        challenge.withdraw()

        assertEquals(euroOf(0), challenge.billableAmount())
    }

    @Test
    fun whenChallengeIsRejected_thenBillableAmountIsZero() {
        val challenge = Challenge(mock(Sponsor::class.java), mock(Team::class.java), euroOf(50), "description")

        challenge.reject()

        assertEquals(euroOf(0), challenge.billableAmount())
    }

    @Test
    fun whenSponsorIsRegistered_thenHasRegisteredSponsorIsTrue() {
        val challenge = Challenge(mock(Sponsor::class.java), mock(Team::class.java), euroOf(50), "description")
        assertTrue(challenge.hasRegisteredSponsor())
    }

    fun whenUnregisteredSponsorAndUsernameInTeam_thenChallengeCanBeWithdrawn() {
        val username = "sponso@example.com"
        val sponsor = mock(UnregisteredSponsor::class.java)
        val team = mock(Team::class.java)

        `when`(team.isMember(username)).thenReturn(true)

        val challenge = Challenge(sponsor, team, euroOf(0), "description")

        assertTrue(challenge.checkWithdrawPermissions(username))
    }

    fun whenUnregisteredSponsorAndUsernameNotInTeam_thenChallengeCantBeWithdrawn() {
        val username = "sponso@example.com"
        val sponsor = mock(UnregisteredSponsor::class.java)
        val team = mock(Team::class.java)

        `when`(team.isMember(username)).thenReturn(false)

        val challenge = Challenge(sponsor, team, euroOf(0), "description")

        assertFalse(challenge.checkWithdrawPermissions(username))
    }

    fun whenRegisteredAndSponsorEmailIsUsername_thenChallengeCanBeWithdrawn() {
        val username = "sponsor@example.com"
        val sponsor = mock(Sponsor::class.java)
        `when`(sponsor.email).thenReturn(username)

        val challenge = Challenge(sponsor, mock(Team::class.java), euroOf(0.0), "description")

        assertTrue(challenge.checkWithdrawPermissions(username))
    }

    fun whenRegisteredAndSponsorEmailIsNotUsername_thenChallengeCantBeWithdrawn() {
        val username = "sponsor@example.com"
        val sponsor = mock(Sponsor::class.java)
        `when`(sponsor.email).thenReturn(username)

        val challenge = Challenge(sponsor, mock(Team::class.java), euroOf(0.0), "description")

        assertFalse(challenge.checkWithdrawPermissions(username))
    }
}
