package backend.model.payment

import backend.Integration.IntegrationTest
import backend.model.event.Event
import backend.model.event.Team
import backend.model.misc.Coord
import backend.model.misc.EmailAddress
import backend.model.user.Admin
import backend.model.user.Participant
import backend.model.user.User
import backend.util.euroOf
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import javax.transaction.Transactional
import kotlin.test.assertEquals


open class TeamEntryFeeServiceImplTest : IntegrationTest() {

    private lateinit var team: Team
    private lateinit var user: User
    private lateinit var admin: Admin
    private lateinit var creator: Participant
    private lateinit var invitee: Participant
    private lateinit var event: Event

    @Before
    override fun setUp() {
        super.setUp()
        user = userService.create("creator@mail.de", "password", { addRole(Participant::class) })
        creator = user.getRole(Participant::class)!!
        invitee = userService.create("invitee@mail.de", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        admin = userService.create("admin@mail.com", "password", { addRole(Admin::class) }).getRole(Admin::class)!!
        event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 1.1), 36)
        team = teamService.create(creator, "name", "description", event, null)

        setAuthenticatedUser(creator.email)
        teamService.invite(EmailAddress(invitee.email), team)

        setAuthenticatedUser(invitee.email)
        teamService.join(invitee, team)
    }

    @Test
    @Transactional
    open fun testAddAdminPaymentToInvoice() {
        val invoice = team.invoice ?: throw Exception("Test failed because team has no invoice. Fix this first!")
        teamEntryFeeService.addAdminPaymentToInvoice(admin, euroOf(60), invoice)
        val foundTeam = teamService.findOne(team.id!!)
        assertEquals(1, foundTeam!!.invoice!!.getPayments().count())
        assertEquals(euroOf(60), foundTeam.invoice!!.getPayments().first().amount)
    }
}
