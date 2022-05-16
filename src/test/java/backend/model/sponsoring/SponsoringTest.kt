package backend.model.sponsoring

import backend.model.event.Event
import backend.model.event.Team
import backend.model.sponsoring.SponsoringStatus.*
import backend.model.user.Sponsor
import backend.util.euroOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import kotlin.test.assertEquals
import kotlin.test.assertFails

@RunWith(PowerMockRunner::class)
@PrepareForTest(Event::class, Team::class, Sponsor::class)
class SponsoringTest {

    @Before
    fun setUp() {

    }

    @Test
    fun testGetAmountPerKm() {
        val event = PowerMockito.mock(Event::class.java)
        val team = PowerMockito.mock(Team::class.java)
        val amountPerKm = euroOf(0.01)
        val limit = euroOf(100)
        val sponsor = PowerMockito.mock(Sponsor::class.java)
        val sponsoring = Sponsoring(event, sponsor, mutableSetOf(team), amountPerKm, limit)

        assertEquals(euroOf(0.01), sponsoring.amountPerKm)
    }

    @Test
    fun testGetLimit() {
        val event = PowerMockito.mock(Event::class.java)
        val team = PowerMockito.mock(Team::class.java)
        val amountPerKm = euroOf(0.01)
        val limit = euroOf(100)
        val sponsor = PowerMockito.mock(Sponsor::class.java)
        val sponsoring = Sponsoring(event, sponsor, mutableSetOf(team), amountPerKm, limit)

        assertEquals(euroOf(100), sponsoring.limit)
    }

    @Test
    fun testGetTeam() {
        val event = PowerMockito.mock(Event::class.java)
        val team = PowerMockito.mock(Team::class.java)
        val amountPerKm = euroOf(0.01)
        val limit = euroOf(100)
        val sponsor = PowerMockito.mock(Sponsor::class.java)
        val sponsoring = Sponsoring(event, sponsor, mutableSetOf(team), amountPerKm, limit)

        //        assertEquals(team, sponsoring.team)
    }

    @Test
    fun testAccept() {
        val event = PowerMockito.mock(Event::class.java)
        val team = PowerMockito.mock(Team::class.java)
        val amountPerKm = euroOf(1.0)
        val limit = euroOf(100)
        val sponsor = PowerMockito.mock(Sponsor::class.java)
        val sponsoring = Sponsoring(event, sponsor, mutableSetOf(team), amountPerKm, limit)

        assertEquals(ACCEPTED, sponsoring.status)
        sponsoring.accept()
        assertEquals(ACCEPTED, sponsoring.status)
    }

    @Test
    fun testMulipleAccept() {
        val event = PowerMockito.mock(Event::class.java)
        val team = PowerMockito.mock(Team::class.java)
        val amountPerKm = euroOf(1.0)
        val limit = euroOf(100)
        val sponsor = PowerMockito.mock(Sponsor::class.java)
        val sponsoring = Sponsoring(event, sponsor, mutableSetOf(team), amountPerKm, limit)

        assertEquals(ACCEPTED, sponsoring.status)
        sponsoring.accept()
        assertEquals(ACCEPTED, sponsoring.status)
    }

    @Test
    fun testReject() {
        val event = PowerMockito.mock(Event::class.java)
        val team = PowerMockito.mock(Team::class.java)
        val amountPerKm = euroOf(1.0)
        val limit = euroOf(100)
        val sponsor = PowerMockito.mock(Sponsor::class.java)
        val sponsoring = Sponsoring(event, sponsor, mutableSetOf(team), amountPerKm, limit)

        assertEquals(ACCEPTED, sponsoring.status)
        sponsoring.reject()
        assertEquals(REJECTED, sponsoring.status)
    }

    @Test
    fun testWithdraw() {
        val event = PowerMockito.mock(Event::class.java)
        val team = PowerMockito.mock(Team::class.java)
        val amountPerKm = euroOf(1.0)
        val limit = euroOf(100)
        val sponsor = PowerMockito.mock(Sponsor::class.java)
        val sponsoring = Sponsoring(event, sponsor, mutableSetOf(team), amountPerKm, limit)

        sponsoring.withdraw()

        assertEquals(WITHDRAWN, sponsoring.status)
        assertFails { sponsoring.accept() }
        assertFails { sponsoring.reject() }
    }

    @Test
    fun billableAmount() {
        val event = PowerMockito.mock(Event::class.java)
        val team = PowerMockito.mock(Team::class.java)
        val sponsor = PowerMockito.mock(Sponsor::class.java)

        PowerMockito.`when`(team.getCurrentDistance()).thenReturn(10.0)

        val sponsoring = Sponsoring(event, sponsor, mutableSetOf(team), euroOf(10.0), euroOf(200))
        sponsoring.accept()

        assertEquals(euroOf(100.0), sponsoring.billableAmount())
    }

    @Test
    fun whenLimitReached_thenBillableAmountIsLimit() {
        val event = PowerMockito.mock(Event::class.java)
        val team = PowerMockito.mock(Team::class.java)
        val sponsor = PowerMockito.mock(Sponsor::class.java)

        PowerMockito.`when`(team.getCurrentDistance()).thenReturn(10.0)

        val sponsoring = Sponsoring(event, sponsor, mutableSetOf(team), euroOf(100.0), euroOf(200))
        sponsoring.accept()

        assertEquals(euroOf(200.0), sponsoring.billableAmount())
    }
}

