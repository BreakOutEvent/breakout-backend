package backend.model.sponsoring

import backend.Integration.IntegrationTest
import backend.model.misc.Coord
import backend.model.sponsoring.SponsoringStatus.*
import backend.model.user.Address
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
    fun emailEventEnded() {
        sponsoringService.sendEmailsToSponsorsWhenEventHasEnded()
    }

    @Test
    fun testCreateSponsoring() {
        val sponsor = userService.create("sponsor@mail.de", "password", { addRole(Sponsor::class) }).getRole(Sponsor::class)!!
        val creator = userService.create("creator@mail.de", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 1.1), 36)
        val team = teamService.create(creator, "name", "description", event, null)
        val sponsoring = sponsoringService.createSponsoring(sponsor, team, euroOf(10), euroOf(20))

        val found = sponsoringRepository.findOne(sponsoring.id)

        assertEquals(sponsoring.id, found.id)
    }

    @Test
    fun testAcceptSponsoring() {
        val sponsor = userService.create("sponsor@mail.de", "password", { addRole(Sponsor::class) }).getRole(Sponsor::class)!!
        val creator = userService.create("creator@mail.de", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 1.1), 36)
        val team = teamService.create(creator, "name", "description", event, null)
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
        val team = teamService.create(creator, "name", "description", event, null)
        val sponsoring = sponsoringService.createSponsoring(sponsor, team, euroOf(10), euroOf(20))

        setAuthenticatedUser(sponsor.email)

        assertFails { sponsoringService.acceptSponsoring(sponsoring) }

        val found = sponsoringRepository.findOne(sponsoring.id)
        assertEquals(ACCEPTED, found.status)

    }

    @Test
    fun testRejectSponsoring() {
        val sponsor = userService.create("sponsor@mail.de", "password", { addRole(Sponsor::class) }).getRole(Sponsor::class)!!
        val creator = userService.create("creator@mail.de", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 1.1), 36)
        val team = teamService.create(creator, "name", "description", event, null)
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
        val team = teamService.create(creator, "name", "description", event, null)
        val sponsoring = sponsoringService.createSponsoring(sponsor, team, euroOf(10), euroOf(20))

        setAuthenticatedUser(sponsor.email)

        assertFails { sponsoringService.rejectSponsoring(sponsoring) }

        val found = sponsoringRepository.findOne(sponsoring.id)
        assertEquals(ACCEPTED, found.status)
    }

    @Test
    fun testWithdrawSponsoringWithUnregisteredSponsorAsTeamMember() {
        val creator = userService.create("creator@mail.de", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 1.1), 36)
        val team = teamService.create(creator, "name", "description", event, null)

        val unregisteredSponsor = UnregisteredSponsor(
                firstname = "Firstname",
                lastname = "Lastname",
                url = "www.url.de",
                company = "company",
                gender = "gender",
                address = Address(street = "street", housenumber = "5", zipcode = "10", city = "city", country = "country")
        )

        setAuthenticatedUser(creator.email)
        val sponsoring = sponsoringService.createSponsoringWithOfflineSponsor(team, euroOf(10), euroOf(100), unregisteredSponsor)
        sponsoringService.withdrawSponsoring(sponsoring)
    }

    @Test
    fun testWithdrawSponsoringWithUnregisteredSponsorAsSponsorFails() {
        val sponsor = userService.create("sponsor@mail.de", "password", { addRole(Sponsor::class) }).getRole(Sponsor::class)!!
        val creator = userService.create("creator@mail.de", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 1.1), 36)
        val team = teamService.create(creator, "name", "description", event, null)

        val unregisteredSponsor = UnregisteredSponsor(
                firstname = "Firstname",
                lastname = "Lastname",
                url = "www.url.de",
                company = "company",
                gender = "gender",
                address = Address(street = "street", housenumber = "5", zipcode = "10", city = "city", country = "country")
        )

        setAuthenticatedUser(creator.email)
        val sponsoring = sponsoringService.createSponsoringWithOfflineSponsor(team, euroOf(10), euroOf(100), unregisteredSponsor)

        setAuthenticatedUser(sponsor.email)
        assertFails { sponsoringService.withdrawSponsoring(sponsoring) }
    }

    @Test
    fun testWithdrawSponsoringWithRegisteredSponsorAsSponsor() {
        val sponsor = userService.create("sponsor@mail.de", "password", { addRole(Sponsor::class) }).getRole(Sponsor::class)!!
        val creator = userService.create("creator@mail.de", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 1.1), 36)
        val team = teamService.create(creator, "name", "description", event, null)
        val sponsoring = sponsoringService.createSponsoring(sponsor, team, euroOf(10), euroOf(20))

        setAuthenticatedUser(sponsor.email)

        sponsoringService.withdrawSponsoring(sponsoring)

        val found = sponsoringRepository.findOne(sponsoring.id)
        assertEquals(WITHDRAWN, found.status)
    }

    @Test
    fun testWithdrawSponsoringWithRegisteredSponsorAsTeamMemberFails() {
        val sponsor = userService.create("sponsor@mail.de", "password", { addRole(Sponsor::class) }).getRole(Sponsor::class)!!
        val creator = userService.create("creator@mail.de", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 1.1), 36)
        val team = teamService.create(creator, "name", "description", event, null)
        val sponsoring = sponsoringService.createSponsoring(sponsor, team, euroOf(10), euroOf(20))

        setAuthenticatedUser(creator.email)

        assertFails { sponsoringService.withdrawSponsoring(sponsoring) }

        val found = sponsoringRepository.findOne(sponsoring.id)
        assertEquals(ACCEPTED, found.status)
    }

}
