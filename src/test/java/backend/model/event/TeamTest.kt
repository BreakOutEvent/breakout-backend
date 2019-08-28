package backend.model.event

import backend.exceptions.DomainException
import backend.model.location.Location
import backend.model.misc.Coord
import backend.model.misc.EmailAddress
import backend.model.user.Participant
import backend.model.user.User
import org.javamoney.moneta.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito.mock
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import java.time.LocalDateTime
import kotlin.test.assertFails
import kotlin.test.assertFailsWith

@RunWith(PowerMockRunner::class)
@PrepareForTest(Location::class, Event::class)
class TeamTest {

    lateinit var event: Event
    lateinit var team: Team
    lateinit var creator: Participant

    @Before
    fun setUp() {
        creator = User.create("creator@mail.de", "password").addRole(Participant::class)
        event = Event("Awesome Event", LocalDateTime.now(), "Munich", Coord(0.0, 0.0), duration = 36, teamFee = Money.of(60, "EUR"), brand = "BreakOut")
        team = Team(creator, "Team awesome", "our team is awesome", event, null)
    }


    @Test
    fun testCreateTeam() {
        val creator = User.create("creator@mail.de", "password").addRole(Participant::class)
        val team = Team(creator, "Team awesome", "our team is awesome", event, null)

        assertEquals(team, creator.getCurrentTeam())
        assertEquals(team.members.size, 1)
        assertTrue(team.members.contains(creator))
    }

    @Test
    fun failToCreateTeam() {
        assertFailsWith<Exception>("Participant ${creator.email} is already part of a team", {
            Team(creator, "Team not Awesome", "our team sucks", event, null)
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
    fun testUserEventsSetCorrectly() {
        val user = User.create("firstname@example.com", "pw").addRole(Participant::class)
        val mockEvent1 = mock(Event::class.java)
        val mockEvent2 = mock(Event::class.java)
        val team1 = Team(user, "", "", mockEvent1, null)
        val team2 = Team(user, "", "", mockEvent2, null)

        assertEquals(team2, user.getCurrentTeam())
        assertTrue(user.getAllTeams().contains(team1))
        assertTrue(user.getAllTeams().contains(team2))
    }

    @Test
    fun testCantJoinMultipleTeamsAtSameEvent() {
        val user = User.create("firstname@example.com", "pw").addRole(Participant::class)
        val mockEvent = mock(Event::class.java)
        val team1 = Team(user, "", "", mockEvent, null)

        assertFails { Team(user, "", "", mockEvent, null) }
        assertEquals(team1, user.getCurrentTeam())
        assertTrue(user.getAllTeams().contains(team1))
        assertEquals(1, user.getAllTeams().size)
    }

    @Test
    fun givenThatAUserHasBeenInATeamAtAnEvent_whenCreatingANewTeamForThatEvent_thenAnExceptionOccurs() {
        val anotherEvent = mock(Event::class.java)
        val anotherTeam = Team(creator, "", "", anotherEvent, null)

        assertFailsWith(DomainException::class) {
            Team(creator, "", "", event, null)
        }
    }

    @Test
    fun whenNoLocation_thenGetCurrentDistanceReturnsZero() {
        assertEquals(0.0, team.getCurrentDistance(), 0.0)
    }
}
