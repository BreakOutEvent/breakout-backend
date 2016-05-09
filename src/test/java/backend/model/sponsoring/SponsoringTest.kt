package backend.model.sponsoring

import backend.model.event.Team
import backend.model.sponsoring.SponsoringStatus.*
import backend.model.user.Sponsor
import org.javamoney.moneta.Money
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(PowerMockRunner::class)
@PrepareForTest(Team::class, Sponsor::class)
class SponsoringTest {

    @Before
    fun setUp() {

    }

    @Test
    fun testGetAmountPerKm() {
        val team = PowerMockito.mock(Team::class.java)
        val amountPerKm = Money.parse("EUR 0.01")
        val limit = Money.parse("EUR 100")
        val sponsor = PowerMockito.mock(Sponsor::class.java)
        val sponsoring = Sponsoring(sponsor, team, amountPerKm, limit)

        assertEquals(Money.parse("EUR 0.01"), sponsoring.amountPerKm)
    }

    @Test
    fun testGetLimit() {
        val team = PowerMockito.mock(Team::class.java)
        val amountPerKm = Money.parse("EUR 0.01")
        val limit = Money.parse("EUR 100")
        val sponsor = PowerMockito.mock(Sponsor::class.java)
        val sponsoring = Sponsoring(sponsor, team, amountPerKm, limit)

        assertEquals(Money.parse("EUR 100"), sponsoring.limit)
    }

    @Test
    fun testGetTeam() {
        val team = PowerMockito.mock(Team::class.java)
        val amountPerKm = Money.parse("EUR 0.01")
        val limit = Money.parse("EUR 100")
        val sponsor = PowerMockito.mock(Sponsor::class.java)
        val sponsoring = Sponsoring(sponsor, team, amountPerKm, limit)

//        assertEquals(team, sponsoring.team)
    }

    @Test
    fun testCalculateRaisedAmount() {
        val team = PowerMockito.mock(Team::class.java)
        val amountPerKm = Money.parse("EUR 0.01")
        val limit = Money.parse("EUR 100")
        val sponsor = PowerMockito.mock(Sponsor::class.java)
        val sponsoring = Sponsoring(sponsor, team, amountPerKm, limit)

        PowerMockito.`when`(team.getMaximumLinearDistanceKM()).thenReturn(63.2)

        assertEquals(Money.parse("EUR 0.632"), sponsoring.calculateRaisedAmount())
    }

    @Test
    fun testCalculateRaisedAmountWhenExceedingLimit() {
        val team = PowerMockito.mock(Team::class.java)
        val amountPerKm = Money.parse("EUR 1.0")
        val limit = Money.parse("EUR 10")
        val sponsor = PowerMockito.mock(Sponsor::class.java)
        val sponsoring = Sponsoring(sponsor, team, amountPerKm, limit)

        PowerMockito.`when`(team.getMaximumLinearDistanceKM()).thenReturn(63.2)

        assertEquals(limit, sponsoring.calculateRaisedAmount())
    }

    @Test
    fun testReachedLimitTrue() {
        val team = PowerMockito.mock(Team::class.java)
        val amountPerKm = Money.parse("EUR 1.0")
        val limit = Money.parse("EUR 10")
        val sponsor = PowerMockito.mock(Sponsor::class.java)
        val sponsoring = Sponsoring(sponsor, team, amountPerKm, limit)

        PowerMockito.`when`(team.getMaximumLinearDistanceKM()).thenReturn(20.0)

        assertTrue(sponsoring.reachedLimit())
    }

    @Test
    fun testReachedLimitFalse() {
        val team = PowerMockito.mock(Team::class.java)
        val amountPerKm = Money.parse("EUR 1.0")
        val limit = Money.parse("EUR 100")
        val sponsor = PowerMockito.mock(Sponsor::class.java)
        val sponsoring = Sponsoring(sponsor, team, amountPerKm, limit)

        PowerMockito.`when`(team.getMaximumLinearDistanceKM()).thenReturn(20.0)

        assertFalse(sponsoring.reachedLimit())
    }

    @Test
    fun testAccept() {
        val team = PowerMockito.mock(Team::class.java)
        val amountPerKm = Money.parse("EUR 1.0")
        val limit = Money.parse("EUR 100")
        val sponsor = PowerMockito.mock(Sponsor::class.java)
        val sponsoring = Sponsoring(sponsor, team, amountPerKm, limit)

        assertEquals(PROPOSED, sponsoring.status)
        sponsoring.accept()
        assertEquals(ACCEPTED, sponsoring.status)
    }

    @Test
    fun testReject() {
        val team = PowerMockito.mock(Team::class.java)
        val amountPerKm = Money.parse("EUR 1.0")
        val limit = Money.parse("EUR 100")
        val sponsor = PowerMockito.mock(Sponsor::class.java)
        val sponsoring = Sponsoring(sponsor, team, amountPerKm, limit)

        assertEquals(PROPOSED, sponsoring.status)
        sponsoring.reject()
        assertEquals(REJECTED, sponsoring.status)
    }
}

