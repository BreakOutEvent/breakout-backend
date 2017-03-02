package backend.model.location

import backend.model.event.Event
import backend.model.event.Team
import backend.model.misc.Coord
import backend.model.user.Participant
import backend.util.distanceCoordsKM
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import java.time.LocalDateTime
import kotlin.test.assertNull

@RunWith(PowerMockRunner::class)
@PrepareForTest(Participant::class, Team::class, Event::class)
class LocationTest {

    private lateinit var event: Event
    private lateinit var coord: Coord
    private lateinit var participant: Participant
    private lateinit var team: Team
    private lateinit var location: Location
    private lateinit var date: LocalDateTime

    @Before
    fun setUp() {

        coord = Coord(0.0, 1.1)

        team = PowerMockito.mock(Team::class.java)
        participant = PowerMockito.mock(Participant::class.java)
        event = PowerMockito.mock(Event::class.java)
        date = PowerMockito.mock(LocalDateTime::class.java)

        Mockito.`when`(participant.getCurrentTeam()).thenReturn(team)
        Mockito.`when`(team.event).thenReturn(event)
        Mockito.`when`(event.startingLocation).thenReturn(Coord(100.0, 203.7))

        location = Location(coord, participant, date, mapOf("COUNTRY" to "Germany"))
    }

    @Test
    fun testGetPoint() {
        assertEquals(coord, location.coord)
    }

    @Test
    fun testGetUploader() {
        assertEquals(participant, location.uploader)
    }

    @Test
    fun testGetTeam() {
        assertEquals(team, location.team)
    }

    @Test
    fun testGetCoord() {
        assertEquals(coord, location.coord)
    }

    @Test
    fun testGetPosting() {
        assertNull(location.posting)
    }

    @Test
    fun testGetDate() {
        assertEquals(date, location.date)
    }

    @Test
    fun testGetDistance() {
        val distance = distanceCoordsKM(from = Coord(100.0, 203.7), to = coord)
        assertEquals(distance, location.distance, 0.0)
    }
}
