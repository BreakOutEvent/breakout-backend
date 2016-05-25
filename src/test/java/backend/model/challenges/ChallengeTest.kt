package backend.model.challenges

import backend.exceptions.DomainException
import backend.model.challenges.ChallengeStatus.*
import backend.model.event.Team
import backend.model.posting.Posting
import backend.model.sponsoring.UnregisteredSponsor
import backend.model.user.Sponsor
import backend.util.euroOf
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@RunWith(PowerMockRunner::class)
@PrepareForTest(Sponsor::class, Team::class, Posting::class, UnregisteredSponsor::class)
class ChallengeTest {

    @Test
    fun testSetSponsor() {
        val sponsor = PowerMockito.mock(Sponsor::class.java)
        val team = PowerMockito.mock(Team::class.java)

        val challenge = Challenge(sponsor, team, euroOf(50), "description")

        assertEquals(sponsor, challenge.sponsor)
        assertNull(challenge.unregisteredSponsor)
    }

    @Test
    fun testFailToSetSponsorWhenUnregisteredSponsorExists() {
        val sponsor = PowerMockito.mock(Sponsor::class.java)
        val unregistered = PowerMockito.mock(UnregisteredSponsor::class.java)
        val team = PowerMockito.mock(Team::class.java)

        val challenge = Challenge(unregistered, team, euroOf(50), "description")

        assertFailsWith(DomainException::class, { challenge.sponsor = sponsor })
    }

    @Test
    fun testSetUnregisteredSponsor() {
        val unregistered = PowerMockito.mock(UnregisteredSponsor::class.java)
        val team = PowerMockito.mock(Team::class.java)

        val challenge = Challenge(unregistered, team, euroOf(50), "description")

        assertEquals(unregistered, challenge.unregisteredSponsor)
        assertNull(challenge.sponsor)
    }

    @Test
    fun testAccept() {
        val sponsor = PowerMockito.mock(Sponsor::class.java)
        val team = PowerMockito.mock(Team::class.java)

        val challenge = Challenge(sponsor, team, euroOf(50), "description")

        assertEquals(PROPOSED, challenge.status)
        challenge.accept()
        assertEquals(ACCEPTED, challenge.status)
    }

    @Test
    fun testMutipleAccept() {
        val sponsor = PowerMockito.mock(Sponsor::class.java)
        val team = PowerMockito.mock(Team::class.java)

        val challenge = Challenge(sponsor, team, euroOf(50), "description")

        assertEquals(PROPOSED, challenge.status)
        challenge.accept()
        challenge.accept()
        assertEquals(ACCEPTED, challenge.status)
    }

    @Test
    fun testReject() {
        val sponsor = PowerMockito.mock(Sponsor::class.java)
        val team = PowerMockito.mock(Team::class.java)

        val challenge = Challenge(sponsor, team, euroOf(50), "description")

        assertEquals(PROPOSED, challenge.status)
        challenge.reject()
        assertEquals(REJECTED, challenge.status)
    }

    @Test
    fun testAddProof() {
        val sponsor = PowerMockito.mock(Sponsor::class.java)
        val team = PowerMockito.mock(Team::class.java)
        val proof = PowerMockito.mock(Posting::class.java)

        val challenge = Challenge(sponsor, team, euroOf(50), "description")
        challenge.accept()

        challenge.addProof(proof)
        assertEquals(proof, challenge.proof)
        assertEquals(WITH_PROOF, challenge.status)
    }

    @Test
    fun testAcceptProof() {
        val sponsor = PowerMockito.mock(Sponsor::class.java)
        val team = PowerMockito.mock(Team::class.java)
        val proof = PowerMockito.mock(Posting::class.java)

        val challenge = Challenge(sponsor, team, euroOf(50), "description")
        challenge.accept()
        challenge.addProof(proof)
        challenge.acceptProof()
        assertEquals(PROOF_ACCEPTED, challenge.status)
    }

    @Test
    fun testRejectProof() {
        val sponsor = PowerMockito.mock(Sponsor::class.java)
        val team = PowerMockito.mock(Team::class.java)
        val proof = PowerMockito.mock(Posting::class.java)

        val challenge = Challenge(sponsor, team, euroOf(50), "description")
        challenge.accept()
        challenge.addProof(proof)
        challenge.rejectProof()
        assertEquals(PROOF_REJECTED, challenge.status)
    }

    @Test
    fun testWithdraw() {
        val team = PowerMockito.mock(Team::class.java)
        val sponsor = PowerMockito.mock(Sponsor::class.java)
        val challenge = Challenge(sponsor, team, euroOf(50), "description")

        challenge.withdraw()

        assertEquals(WITHDRAWN, challenge.status)
        assertFails { challenge.accept() }
        assertFails { challenge.reject() }
    }

}
