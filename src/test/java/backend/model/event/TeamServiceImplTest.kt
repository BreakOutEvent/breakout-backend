package backend.model.event

import backend.Integration.IntegrationTest
import backend.model.event.Invitation.InvitationStatus.OPEN
import backend.model.user.Participant
import backend.model.user.UserService
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.collections.first
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TeamServiceImplTest : IntegrationTest() {

    @Autowired
    private lateinit var repository: TeamRepository

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var service: TeamService

    @Test
    fun testCreate() {
        val user = userService.create("f@x.de", "lorem")
        user.addRole(Participant::class.java)
        userService.save(user)

        val participant = userService.getAllUsers()!!.first().getRole(Participant::class.java) as Participant

        val team = service.create(participant, "Team Awesome", "Das beste Team aus Dresden")
        val savedTeam = repository.findAll().first()

        assertNotNull(savedTeam)
        assertEquals(team.id, savedTeam.id)
        assertEquals(participant, team.members.first())
    }

    @Test
    fun testInvite() {
        val creator = userService.create("a@b.c", "lorem").addRole(Participant::class.java)
        userService.save(creator)
        val creatorParticipant = userService.getUserByEmail("a@b.c")!!.getRole(Participant::class.java) as Participant

        val team = service.create(creatorParticipant, "Team awesome", "Our team is the best!!")
        service.invite("b@b.c", team)

        assertNotNull(team.invitation)
        assertEquals(team.invitation!!.status, OPEN)
        assertEquals(team.invitation!!.invitee, "b@b.c")

        val foundTeam = repository.findAll().first()
        assertNotNull(foundTeam.invitation)
        assertEquals(foundTeam.invitation!!.status, OPEN)
        assertEquals(foundTeam.invitation!!.invitee, "b@b.c")
    }
}
