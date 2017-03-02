package backend.model.event

import backend.model.location.Location
import backend.model.misc.Coord
import backend.model.misc.EmailAddress
import backend.model.user.Participant
import backend.model.user.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.powermock.api.mockito.PowerMockito.mock
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import java.time.LocalDateTime
import kotlin.test.assertFails
import kotlin.test.assertFailsWith

@RunWith(PowerMockRunner::class)
@PrepareForTest(Location::class)
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

        assertEquals(team, creator.getCurrentTeam())
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
    fun testFailToJoinIfNotInvited() {
        val inviteeEmail = EmailAddress("invitee@mail.com")
        val notInvitee = User.create("notinvitee@mail.com", "password").addRole(Participant::class)
        team.invite(inviteeEmail)

        assertFails({ team.join(notInvitee) })
    }

    @Test
    fun testJoinWithEmailInDifferentCase1() {
        val inviteeEmail = EmailAddress("invitee@mail.com")
        val invitee = User.create("INVITEE@mail.com", "password").addRole(Participant::class)

        team.invite(inviteeEmail)
        team.join(invitee)
    }

    @Test
    fun testJoinWithEmailInDifferentCase2() {
        val inviteeEmail = EmailAddress("INVITEE@mail.com")
        val invitee = User.create("invitee@mail.com", "password").addRole(Participant::class)

        team.invite(inviteeEmail)
        team.join(invitee)
    }

    @Test
    fun testFailToJoinIfTeamAlreadyFull() {
        val inviteeEmail = EmailAddress("invitee@mail.com")
        val secondInvitee = EmailAddress("secondInvitee@mail.com")

        team.invite(inviteeEmail)
        team.invite(secondInvitee)

        val firstUser = User.create(inviteeEmail.toString(), "password").addRole(Participant::class)
        val secondUser = User.create(secondInvitee.toString(), "password").addRole(Participant::class)

        team.join(firstUser)
        assertFails { team.join(secondUser) }
    }

    @Test
    fun testInvite() {
        val firstInvitee = EmailAddress("invitee@mail.de")
        val secondInvitee = EmailAddress("second@mail.de")

        team.invite(firstInvitee)
        team.invite(secondInvitee)
    }

    @Test
    fun testFailToInviteIfAlreadyInvited() {
        val firstInvitee = EmailAddress("invitee@mail.de")
        val secondInvitee = EmailAddress("second@mail.de")

        team.invite(firstInvitee)
        team.invite(secondInvitee)

        assertFails { team.invite(secondInvitee) }
    }

    @Test
    fun testIsInvited() {
        val firstInvitee = EmailAddress("invitee@mail.de")
        val secondInvitee = EmailAddress("second@mail.de")

        team.invite(firstInvitee)
        team.invite(secondInvitee)

        assertTrue(team.isInvited(firstInvitee))
        assertTrue(team.isInvited(secondInvitee))
    }

    @Test
    fun testIsFull() {
        val firstInvitee = EmailAddress("invitee@mail.de")
        val invitee = User.create(firstInvitee.toString(), "password").addRole(Participant::class)
        team.invite(firstInvitee)
        team.join(invitee)

        assertTrue(team.isFull())
    }

    @Test
    fun getCurrentDistance() {
        val location1 = mock(Location::class.java)
        val location2 = mock(Location::class.java)
        val location3 = mock(Location::class.java)

        `when`(location1.distance).thenReturn(10.0)
        `when`(location2.distance).thenReturn(20.0)
        `when`(location3.distance).thenReturn(15.0)

        team.locations.addAll(listOf(location1, location2, location3))

        assertEquals(20.0, team.getCurrentDistance(), 0.0)
    }

    @Test
    fun whenNoLocation_thenGetCurrentDistanceReturnsZero() {
        assertEquals(0.0, team.getCurrentDistance(), 0.0)
    }
}
