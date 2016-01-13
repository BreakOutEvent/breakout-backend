package backend.model.event

import backend.model.user.Participant
import backend.model.user.UserCore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertFailsWith

class TeamTest {

    @Test
    fun testCreateTeam() {
        val user1 = UserCore()
        user1.addRole(Participant::class.java)
        val participant = user1.getRole(Participant::class.java) as Participant

        val team = Team(participant, "Team awesome", "our team is awesome")

        assertEquals(team, participant.currentTeam)
        assertEquals(team.members.size, 1)
        assertTrue(team.members.contains(participant))
    }

    @Test
    fun testAddMember() {
        val user1 = UserCore()
        user1.addRole(Participant::class.java)
        val participant1 = user1.getRole(Participant::class.java) as Participant

        val user2 = UserCore()
        user2.addRole(Participant::class.java)
        val participant2 = user2.getRole(Participant::class.java) as Participant

        val team = Team(participant1, "Team awesome", "our team is awesome")

        team.addMember(participant2)

        assertEquals(team, participant2.currentTeam)
        assertEquals(team.members.size, 2)
        assertTrue(team.members.contains(participant2))
    }

    @Test
    fun failToAddMemberBecauseTeamIsFull() {
        val user1 = UserCore()
        user1.addRole(Participant::class.java)
        val participant1 = user1.getRole(Participant::class.java) as Participant

        val user2 = UserCore()
        user2.addRole(Participant::class.java)
        val participant2 = user2.getRole(Participant::class.java) as Participant

        val user3 = UserCore()
        user1.addRole(Participant::class.java)
        val participant3 = user1.getRole(Participant::class.java) as Participant

        val team = Team(participant1, "Team awesome", "our team is awesome")

        team.addMember(participant2)

        assertFailsWith<Exception>("This team already has two members", {
            team.addMember(participant3)
        })

    }
}
