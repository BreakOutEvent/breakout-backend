package backend.model.location

import backend.model.event.Team
import backend.model.user.Participant
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import java.time.LocalDateTime

@RunWith(PowerMockRunner::class)
@PrepareForTest(Participant::class, Team::class)
class LocationTest {

    private lateinit var point: Point
    private lateinit var participant: Participant
    private lateinit var team: Team
    private lateinit var location: Location
    private lateinit var date: LocalDateTime

    @Before
    fun setUp() {
        team = PowerMockito.mock(Team::class.java)
        point = Point(0.0, 1.1)
        participant = PowerMockito.mock(Participant::class.java)

        Mockito.`when`(participant.currentTeam).thenReturn(team)
        date = PowerMockito.mock(LocalDateTime::class.java)
        location = Location(point, participant, date)
    }

    @Test
    fun testGetPoint() {
        assertEquals(point, location.point)
    }

    @Test
    fun testGetUploader() {
        assertEquals(participant, location.uploader)
    }

    @Test
    fun testGetTeam() {
        assertEquals(team, location.team)
    }
}
