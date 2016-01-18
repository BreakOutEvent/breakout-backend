package backend.model.event

import backend.Integration.IntegrationTest
import backend.model.user.Participant
import backend.model.user.UserCore
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

    }
}
