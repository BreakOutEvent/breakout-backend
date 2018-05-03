package backend.model.payment

import backend.Integration.IntegrationTest
import backend.model.misc.Coord
import backend.model.sponsoring.TeamBuilder
import backend.model.user.Admin
import backend.model.user.Sponsor
import backend.util.euroOf
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SponsoringInvoiceServiceImplTest : IntegrationTest() {

    @Test
    fun findAllNotFullyPaidInvoicesForEvent() {

        // given a team with sponsors and completed challenges at one event
        // with one complete and one partial payment for the sponsors invoices
        val sponsor1 = userService.create("sponsor@example.com", "test", { addRole(Sponsor::class) }).getRole(Sponsor::class)!!
        val sponsor2 = userService.create("sponsor2@example.com", "test", { addRole(Sponsor::class) }).getRole(Sponsor::class)!!
        val admin = userService.create("admin@example.com", "pw", { addRole(Admin::class) }).getRole(Admin::class)!!

        val event = eventService.createEvent("Muc", LocalDateTime.now(), "", Coord(0.0), 36)

        val team = TeamBuilder.createWith(teamService, userService, userDetailsService) {
            name = "Testteam"
            description = ""
            creator {
                firstname = ""
                lastname = ""
                email = "creator@example.com"
                password = "pw"
            }
            invited {
                firstname = ""
                lastname = ""
                email = "invited@example.com"
                password = "pw"
            }
            this.event = event
        }.build()

        setAuthenticatedUser(sponsor1.email)
        val challenge1 = challengeService.proposeChallenge(sponsor1, team, euroOf(10.0), "Do something funny")

        setAuthenticatedUser(sponsor2.email)
        val challenge2 = challengeService.proposeChallenge(sponsor2, team, euroOf(10.0), "Yeah, but my idea is better")

        val firstProof = postingService.createPosting(team.members.first(), "lol", null, null, LocalDateTime.now())
        val secondProof = postingService.createPosting(team.members.first(), "nice", null, null, LocalDateTime.now())

        setAuthenticatedUser(team.members.first().email)
        challengeService.addProof(challenge1, firstProof)
        challengeService.addProof(challenge2, secondProof)

        eventService.regenerateCache(event.id!!)
        sponsoringInvoiceService.createInvoicesForEvent(event)

        val invoices = sponsoringInvoiceService.findAll()
        val first = invoices.toList()[0]
        val second = invoices.toList()[1]

        sponsoringInvoiceService.addAdminPaymentToInvoice(admin, euroOf(10.0), first, null, null)
        sponsoringInvoiceService.addAdminPaymentToInvoice(admin, euroOf(5.0), first, null, null)

        // when looking for not fully paid invoices
        val notFullyPaid = sponsoringInvoiceService.findAllNotFullyPaidInvoicesForEvent(event)

        // then it only contains the one which is not fully paid yet
        assertTrue(notFullyPaid.contains(second))
        assertFalse(notFullyPaid.contains(first))

    }

}
