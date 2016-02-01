package backend.model.event

import backend.Integration.IntegrationTest
import backend.model.event.Invitation.InvitationStatus.OPEN
import backend.model.misc.Coords
import backend.model.misc.EmailAddress
import backend.model.user.Participant
import backend.model.user.User
import backend.model.user.UserService
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import kotlin.collections.first
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TeamServiceImplTest : IntegrationTest() {


    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var eventService: EventService

    @Autowired
    private lateinit var teamService: TeamService

    @Autowired
    private lateinit var repository: TeamRepository
    private lateinit var event: Event

    @Before
    override fun setUp() {
        super.setUp()
        event = eventService.createEvent("BreakOut München", LocalDateTime.now(), "München", Coords(0.0, 1.1), 36)
    }

    @Test
    fun testCreate() {
        val participant = User.create("f@x.de", "lorem").addRole(Participant::class.java) as Participant
        userService.save(participant)

        val team = teamService.create(participant, "Team Awesome", "Das beste Team aus Dresden", event)
        val savedTeam = repository.findAll().first()

        assertNotNull(savedTeam)
        assertEquals(team.id, savedTeam.id)
        assertEquals(participant.core.id, savedTeam.members.first().core.id)
    }

    @Test
    fun testInvite() {
        // Insert MockObjects here
        // Make sure Email is sent
        // Make sure Invitation on Model is called!
    }
}
