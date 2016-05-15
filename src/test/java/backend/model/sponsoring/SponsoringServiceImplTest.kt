package backend.model.sponsoring

import backend.Integration.IntegrationTest
import backend.model.misc.Coord
import backend.model.sponsoring.SponsoringStatus.*
import backend.model.user.Participant
import backend.model.user.Sponsor
import backend.util.euroOf
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFails

class SponsoringServiceImplTest : IntegrationTest() {

    @Before
    override fun setUp() {
        super.setUp()
    }

    @Test
    fun testCreateSponsoring() {
        val sponsor = userService.create("sponsor@mail.de", "password", { addRole(Sponsor::class) }).getRole(Sponsor::class)!!
        val creator = userService.create("creator@mail.de", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 1.1), 36)
        val team = teamService.create(creator, "name", "description", event)
        val sponsoring = sponsoringService.createSponsoring(sponsor, team, euroOf(10), euroOf(20))

        val found = sponsoringRepository.findOne(sponsoring.id)

        assertEquals(sponsoring.id, found.id)
    }

    @Test
    fun testAcceptSponsoring() {
        val sponsor = userService.create("sponsor@mail.de", "password", { addRole(Sponsor::class) }).getRole(Sponsor::class)!!
        val creator = userService.create("creator@mail.de", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 1.1), 36)
        val team = teamService.create(creator, "name", "description", event)
        val sponsoring = sponsoringService.createSponsoring(sponsor, team, euroOf(10), euroOf(20))

        setAuthenticatedUser(creator.email)

        sponsoringService.acceptSponsoring(sponsoring)

        val found = sponsoringRepository.findOne(sponsoring.id)
        assertEquals(ACCEPTED, found.status)
    }

    @Test
    fun testAcceptSponsoringFailsIfNotPartOfTeam() {
        val sponsor = userService.create("sponsor@mail.de", "password", { addRole(Sponsor::class) }).getRole(Sponsor::class)!!
        val creator = userService.create("creator@mail.de", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 1.1), 36)
        val team = teamService.create(creator, "name", "description", event)
        val sponsoring = sponsoringService.createSponsoring(sponsor, team, euroOf(10), euroOf(20))

        setAuthenticatedUser(sponsor.email)

        assertFails { sponsoringService.acceptSponsoring(sponsoring) }

        val found = sponsoringRepository.findOne(sponsoring.id)
        assertEquals(PROPOSED, found.status)

    }

    @Test
    fun testRejectSponsoring() {
        val sponsor = userService.create("sponsor@mail.de", "password", { addRole(Sponsor::class) }).getRole(Sponsor::class)!!
        val creator = userService.create("creator@mail.de", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 1.1), 36)
        val team = teamService.create(creator, "name", "description", event)
        val sponsoring = sponsoringService.createSponsoring(sponsor, team, euroOf(10), euroOf(20))

        setAuthenticatedUser(creator.email)

        sponsoringService.rejectSponsoring(sponsoring)

        val found = sponsoringRepository.findOne(sponsoring.id)
        assertEquals(REJECTED, found.status)
    }

    @Test
    fun testRejectSponsoringFailsIfNotPartOfTeam() {
        val sponsor = userService.create("sponsor@mail.de", "password", { addRole(Sponsor::class) }).getRole(Sponsor::class)!!
        val creator = userService.create("creator@mail.de", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 1.1), 36)
        val team = teamService.create(creator, "name", "description", event)
        val sponsoring = sponsoringService.createSponsoring(sponsor, team, euroOf(10), euroOf(20))

        setAuthenticatedUser(sponsor.email)

        assertFails { sponsoringService.rejectSponsoring(sponsoring) }

        val found = sponsoringRepository.findOne(sponsoring.id)
        assertEquals(PROPOSED, found.status)
    }
}
