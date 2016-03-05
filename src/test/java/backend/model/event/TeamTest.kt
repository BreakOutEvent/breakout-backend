package backend.model.event

import backend.model.misc.Coord
import backend.model.misc.EmailAddress
import backend.model.user.Participant
import backend.model.user.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertFails
import kotlin.test.assertFailsWith

class TeamTest {

    lateinit var event: Event
    lateinit var team: Team
    lateinit var creator: Participant

    @Before
    fun setUp() {
        creator = User.create("creator@mail.de", "password").addRole(Participant::class)
        event = Event("Awesome Event", LocalDateTime.now(), "Munich", Coord(0.0, 0.0), duration = 36)
        team = Team(creator, "Team awesome", "our team is awesome", event)
    }


    @Test
    fun testCreateTeam() {
        val creator = User.create("creator@mail.de", "password").addRole(Participant::class)
        val team = Team(creator, "Team awesome", "our team is awesome", event)

        assertEquals(team, creator.currentTeam)
        assertEquals(team.members.size, 1)
        assertTrue(team.members.contains(creator))
    }

    @Test
    fun failToCreateTeam() {
        assertFailsWith<Exception>("Participant ${creator.email} is already part of a team", {
            Team(creator, "Team not Awesome", "our team sucks", event)
        })
    }

    @Test
    fun testJoin() {
        val inviteeEmail = EmailAddress("invitee@mail.com")
        val invitee = User.create(inviteeEmail.toString(), "password").addRole(Participant::class)

        team.invite(inviteeEmail)
        team.join(invitee)
    }

    @Test
    fun testFailToJoin() {
        val inviteeEmail = EmailAddress("invitee@mail.com")
        val notInvitee = User.create("notinvitee@mail.com", "password").addRole(Participant::class)
        team.invite(inviteeEmail)

        assertFails({ team.join(notInvitee) })
    }

    @Test
    fun testInvite() {
        val firstInvitee = EmailAddress("invitee@mail.de")
        val secondInvitee = EmailAddress("second@mail.de")

        team.invite(firstInvitee)

        assertFails({ team.invite(secondInvitee) })
    }
}
