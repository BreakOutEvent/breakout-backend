package backend.model.sponsoring

import backend.Integration.IntegrationTest
import backend.model.misc.Coord
import backend.model.user.Participant
import backend.model.user.Sponsor
import org.javamoney.moneta.Money
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals

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
        val sponsoring = sponsoringService.createSponsoring(sponsor, team, Money.parse("EUR 10"), Money.parse("EUR 20"))

        val found = sponsoringRepository.findOne(sponsoring.id)

        assertEquals(sponsoring.id, found.id)
    }
}
