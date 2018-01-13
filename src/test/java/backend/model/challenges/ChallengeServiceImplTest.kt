package backend.model.challenges

import backend.Integration.IntegrationTest
import backend.exceptions.DomainException
import backend.model.event.Event
import backend.model.event.Team
import backend.model.misc.Coord
import backend.model.posting.Posting
import backend.model.sponsoring.UnregisteredSponsor
import backend.model.user.Address
import backend.model.user.Participant
import backend.model.user.Sponsor
import backend.services.Feature
import backend.util.euroOf
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

open class ChallengeServiceImplTest : IntegrationTest() {

    private lateinit var team: Team
    private lateinit var participant: Participant
    private lateinit var sponsor: Sponsor
    private lateinit var event: Event
    private lateinit var unregisteredSponsor: UnregisteredSponsor
    private lateinit var posting: Posting

    @Before
    override fun setUp() {
        super.setUp()
        event = eventService.createEvent("Title", LocalDateTime.now(), "Munich", Coord(0.0), 36)
        participant = userService.create("participant@break-out.org", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        sponsor = userService.create("sponsor@break-out.org", "password", { addRole(Sponsor::class) }).getRole(Sponsor::class)!!
        team = teamService.create(participant, "name", "description", event, null)
        posting = postingService.createPosting(participant, "Test", null, null, LocalDateTime.now())
        unregisteredSponsor = UnregisteredSponsor(
                firstname = "firstname",
                lastname = "lastname",
                address = Address("1", "2", "3", "4", "5"),
                url = "www.test.de",
                company = "test",
                gender = "male",
                isHidden = false)
    }

    @Test
    fun testProposeChallengeRegisteredSponsor() {
        setAuthenticatedUser("sponsor@break-out.org")
        val challenge = challengeService.proposeChallenge(sponsor, team, euroOf(50.0), "description")

        val found = challengeRepository.findOne(challenge.id)
        assertNotNull(found)
        assertEquals(sponsor.id, found.sponsor.registeredSponsor!!.id)
    }

    @Test
    fun testProposeChallenge1() {
        setAuthenticatedUser("participant@break-out.org")
        val challenge = challengeService.proposeChallenge(unregisteredSponsor, team, euroOf(50.0), "description")

        val found = challengeRepository.findOne(challenge.id)
        assertNotNull(found)
        assertNotNull(found.sponsor.unregisteredSponsor)
    }

    @Test
    fun testWithdrawChallengeWithUnregisteredSponsorAsTeamMember() {

        setAuthenticatedUser("participant@break-out.org")
        val challenge = challengeService.proposeChallenge(unregisteredSponsor, team, euroOf(100), "desc")
        challengeService.withdraw(challenge)
    }

    @Test
    fun testWithdrawChallengeWithUnregisteredSponsorAsSponsorFails() {
        setAuthenticatedUser("participant@break-out.org")

        val challenge = challengeService.proposeChallenge(unregisteredSponsor, team, euroOf(100), "desc")

        setAuthenticatedUser(sponsor.email)
        assertFails { challengeService.withdraw(challenge) }
    }

    @Test
    fun testWithdrawChallengeWithRegisteredSponsorAsSponsor() {

        setAuthenticatedUser("sponsor@break-out.org")

        val challenge = challengeService.proposeChallenge(sponsor, team, euroOf(20), "desc")

        challengeService.withdraw(challenge)

        val found = challengeService.findOne(challenge.id!!)
        assertEquals(ChallengeStatus.WITHDRAWN, found!!.status)
    }

    @Test
    fun testWithdrawChallengeWithRegisteredSponsorAsTeamMemberFails() {

        setAuthenticatedUser("sponsor@break-out.org")

        val challenge = challengeService.proposeChallenge(sponsor, team, euroOf(20), "desc")

        setAuthenticatedUser("participant@break-out.org")

        assertFails { challengeService.withdraw(challenge) }

        val found = challengeService.findOne(challenge.id!!)
        assertEquals(ChallengeStatus.PROPOSED, found!!.status)
    }

    fun testAddProofFailsIfFeatureDisabled() {

        val feature = Feature("challenge.addProof", false)
        featureRepository.save(feature)

        setAuthenticatedUser("sponsor@break-out.org")
        val challenge = challengeService.proposeChallenge(sponsor, team, euroOf(20), "desc")
        setAuthenticatedUser("participant@break-out.org")

        assertFailsWith<DomainException>("Can't add proof to challenge. Feature disabled") { challenge.addProof() }
    }
}
