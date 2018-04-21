package backend.model.event

import backend.Integration.IntegrationTest
import backend.model.misc.Coord
import backend.model.misc.EmailAddress
import backend.model.user.Participant
import backend.model.user.User
import backend.model.user.UserRole
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

open class TeamServiceImplTest : IntegrationTest() {

    private lateinit var event: Event

    @Before
    override fun setUp() {
        super.setUp()
        event = eventService.createEvent("BreakOut München", LocalDateTime.now(), "München", Coord(0.0, 1.1), 36)
    }

    @Test
    @Transactional
    fun testCreate() {
        val participant = User.create("f@x.de", "lorem").addRole(Participant::class)
        userService.save(participant)

        val team = teamService.create(participant, "Team Awesome", "Das beste Team aus Dresden", event, null)
        val savedTeam = teamRepository.findAll().first()

        assertNotNull(savedTeam)
        assertEquals(team.id, savedTeam.id)
        assertEquals(participant.account.id, savedTeam.members.first().account.id)
    }

    @Test
    fun testInvite() {
        val creator = setAuthenticatedUser("user@mail.com", Participant::class.java).getRole(Participant::class)!!
        val team = teamService.create(creator, "name", "description", event, null)

        teamService.invite(EmailAddress("invitee@mail.de"), team)
    }

    @Test
    fun failToInvite() {
        setAuthenticatedUser("user@mail.com", Participant::class.java).getRole(Participant::class)
        val creator = userService.create("not@mail.com", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val team = teamService.create(creator, "name", "description", event, null)

        assertFails { teamService.invite(EmailAddress("test@mail.com"), team) }
    }

    private fun setAuthenticatedUser(email: String, role: Class<out UserRole>): User {
        val user = userService.create(email, "password", { addRole(role.kotlin) }).getRole(role.kotlin)!!
        val details = userDetailsService.loadUserByUsername(email)!! // Not null because otherwise exception is thrown
        val token = UsernamePasswordAuthenticationToken(details.username, details.password, details.authorities)
        SecurityContextHolder.getContext().authentication = token
        return user
    }

    @Test
    fun testSave() {
        // TODO: Implement tests
    }

    @Test
    fun testGetByID() {
        // TODO: Implement tests
    }

    @Test
    fun testFindPostingsById() {
        // TODO: Implement tests
    }

    @Test
    fun testFindLocationPostingsById() {
        // TODO: Implement tests
    }

    @Test
    fun testGetPostingMaxDistanceById() {
        // TODO: Implement tests
    }

    @Test
    fun testFindInvitationsForUser() {
        // Authenticated as inviting user
        val creator = setAuthenticatedUser("inviting@mail.com", Participant::class.java).getRole(Participant::class)!!
        val team = teamService.create(creator, "Team awesome", "description", event, null)
        teamService.invite(EmailAddress("invitee@mail.com"), team)

        // Authenticated as invitee
        val invitee = setAuthenticatedUser("invitee@mail.com", Participant::class.java).getRole(Participant::class)!!

        // See if all my invitations are there
        val invitations = teamService.findInvitationsForUser(invitee)
        assertNotNull(invitations)
        assertTrue { invitations.map { it.invitee.toString() }.contains(invitee.email) }
    }
}
