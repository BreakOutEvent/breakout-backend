package backend.model.user

import backend.model.event.Event
import backend.model.event.Team
import backend.model.misc.EmailAddress
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito.mock
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@RunWith(PowerMockRunner::class)
@PrepareForTest(Event::class, Participant::class, Team::class)
class ParticipantTest {

    @Test
    fun whenParticipantJoinsATeam_thenThisIsHisCurrentTeam() {
        val user = User.create("email@example.com", "password")
        val participant = user.addRole(Participant::class)

        val team = Team(mock(Participant::class.java), "", "", mock(Event::class.java), null)
        team.invite(EmailAddress(participant.email))
        team.join(participant)

        assertEquals(team, participant.getCurrentTeam())
    }

    @Test
    fun whenParticipantCreatesATeam_thenThisIsHisCurrentTeam() {
        val user = User.create("email@example.com", "password")
        val participant = user.addRole(Participant::class)
        val team = Team(participant, "", "", mock(Event::class.java), null)

        assertEquals(team, participant.getCurrentTeam())
    }

    @Test
    fun whenParticipantCreatesATeam_thenGetAllTeamsContainsThisTeam() {
        val user = User.create("email@example.com", "password")
        val participant = user.addRole(Participant::class)
        val team = Team(participant, "", "", mock(Event::class.java), null)

        assertTrue(participant.getAllTeams().contains(team))
    }

    @Test
    fun whenParticipantJoinsATeam_thenGetAllTeamsContainsThisTeam() {
        val user = User.create("email@example.com", "password")
        val participant = user.addRole(Participant::class)

        val team = Team(mock(Participant::class.java), "", "", mock(Event::class.java), null)
        team.invite(EmailAddress(participant.email))
        team.join(participant)

        assertTrue(participant.getAllTeams().contains(team))
    }

    @Test
    fun setCurrentTeam() {
        val user = User.create("email@example.com", "password")
        val participant = user.addRole(Participant::class)

        val team = mock(Team::class.java)
        participant.setCurrentTeam(team)

        assertEquals(team, participant.getCurrentTeam())
        assertTrue(participant.getAllTeams().contains(team))
    }

    @Test
    fun whenAParticipantAlreadyIsInATeamForAnEvent_thenHeCantJoinAnotherTeamForThisEvent() {
        val event1 = mock(Event::class.java)

        val user = User.create("email@example.com", "password")
        val participant = user.addRole(Participant::class)
        Team(participant, "", "", event1, null)

        val anotherTeam = Team(mock(Participant::class.java), "", "", event1, null)
        anotherTeam.invite(EmailAddress(participant.email))

        assertFails {
            anotherTeam.join(participant)
        }
    }

    @Test
    fun whenAParticipantAlreadyIsInATeamForAnEvent_thenHeCanJoinAnotherTeamForADifferentEvent() {
        val anEvent = mock(Event::class.java)
        val aDifferentEvent = mock(Event::class.java)

        val user = User.create("email@example.com", "password")
        val participant = user.addRole(Participant::class)
        val team = Team(participant, "", "", anEvent, null)

        val anotherTeam = Team(mock(Participant::class.java), "", "", aDifferentEvent, null)
        anotherTeam.invite(EmailAddress(participant.email))


        anotherTeam.join(participant)
        assertTrue(team.isMember(participant))
        assertTrue(anotherTeam.isMember(participant))
        assertEquals(anotherTeam, participant.getCurrentTeam())
        assertNotEquals(team, participant.getCurrentTeam())

        assertTrue(participant.getAllTeams().contains(team))
        assertTrue(participant.getAllTeams().contains(anotherTeam))
    }
}
