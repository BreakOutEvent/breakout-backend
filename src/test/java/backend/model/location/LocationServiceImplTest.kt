package backend.model.location

import backend.Integration.IntegrationTest
import backend.model.event.Event
import backend.model.event.Team
import backend.model.misc.Coord
import backend.model.user.Participant
import backend.model.user.User
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LocationServiceImplTest : IntegrationTest() {

    private lateinit var user: User
    private lateinit var team: Team
    private lateinit var event: Event

    @Before
    override fun setUp() {
        super.setUp()
        this.user = userService.create("email1@test.com", "password", { addRole(Participant::class) })
        val participant = user.getRole(Participant::class)!!
        this.event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(1.0, 1.0), 36)
        this.team = teamService.create(participant, "name", "description", event, null)
    }

    @Test
    fun testFindAll() {
        this.locationService.create(Coord(1.0, 1.0), user.getRole(Participant::class)!!, LocalDateTime.now())
        this.locationService.create(Coord(1.1, 1.1), user.getRole(Participant::class)!!, LocalDateTime.now())
        this.locationService.create(Coord(2.2, 2.2), user.getRole(Participant::class)!!, LocalDateTime.now())

        val results = this.locationService.findAll()
        assertEquals(3, results.count())
        results.forEach {
            assertTrue(it.uploader != null)
        }
    }

    @Test
    fun testSave() {
        val location = Location(Coord(1.0, 1.0), user.getRole(Participant::class)!!, LocalDateTime.now(), mapOf())
        locationService.save(location)

        val foundLocations = locationService.findAll()
        assertEquals(1, foundLocations.count())
    }

    @Test
    fun testCreate() {
        locationService.create(Coord(1.0, 1.0), user.getRole(Participant::class)!!, LocalDateTime.now())

        val foundLocations = locationService.findAll()
        assertEquals(1, foundLocations.count())
    }

    @Ignore //locations are filtered during event
    @Test
    fun testFindByTeamId() {
        val secondUser = userService.create("emai2@test.com", "password", { addRole(Participant::class) })
        val secondParticipant = secondUser.getRole(Participant::class)!!
        val secondTeam = teamService.create(secondParticipant, "other name", "other description", event, null)

        locationService.create(Coord(1.0, 1.0), secondParticipant, LocalDateTime.now())

        val foundLocations = locationService.findByTeamId(secondTeam.id!!, 10)
        assertEquals(1, foundLocations.count())
        assertEquals(secondTeam.id, foundLocations.first().team!!.id)
    }
}
