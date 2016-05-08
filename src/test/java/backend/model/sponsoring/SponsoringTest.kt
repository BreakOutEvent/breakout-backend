package backend.model.sponsoring

import backend.model.event.Team
import org.javamoney.moneta.Money
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(PowerMockRunner::class)
@PrepareForTest(Team::class)
class SponsoringTest {

    @Before
    fun setUp() {

    }

    @Test
    fun testGetAmountPerKm() {
        val team = PowerMockito.mock(Team::class.java)
        val amountPerKm = Money.parse("EUR 0.01")
        val limit = Money.parse("EUR 100")
        val sponsoring = Sponsoring(team, amountPerKm, limit)

        assertEquals(Money.parse("EUR 0.01"), sponsoring.amountPerKm)
    }

    @Test
    fun testGetLimit() {
        val team = PowerMockito.mock(Team::class.java)
        val amountPerKm = Money.parse("EUR 0.01")
        val limit = Money.parse("EUR 100")
        val sponsoring = Sponsoring(team, amountPerKm, limit)

        assertEquals(Money.parse("EUR 100"), sponsoring.limit)
    }

    @Test
    fun testGetTeam() {
        val team = PowerMockito.mock(Team::class.java)
        val amountPerKm = Money.parse("EUR 0.01")
        val limit = Money.parse("EUR 100")
        val sponsoring = Sponsoring(team, amountPerKm, limit)

        assertEquals(team, sponsoring.team)
    }

    @Test
    fun testCalculateRaisedAmount() {
        val team = PowerMockito.mock(Team::class.java)
        val amountPerKm = Money.parse("EUR 0.01")
        val limit = Money.parse("EUR 100")
        val sponsoring = Sponsoring(team, amountPerKm, limit)

        PowerMockito.`when`(team.getLinearDistance()).thenReturn(63200.0)

        assertEquals(Money.parse("EUR 0.632"), sponsoring.calculateRaisedAmount())
    }

    @Test
    fun testCalculateRaisedAmountWhenExceedingLimit() {
        val team = PowerMockito.mock(Team::class.java)
        val amountPerKm = Money.parse("EUR 1.0")
        val limit = Money.parse("EUR 10")
        val sponsoring = Sponsoring(team, amountPerKm, limit)

        PowerMockito.`when`(team.getLinearDistance()).thenReturn(20000.0)

        assertEquals(limit, sponsoring.calculateRaisedAmount())
    }

    @Test
    fun testReachedLimit() {
        val team = PowerMockito.mock(Team::class.java)
        val amountPerKm = Money.parse("EUR 1.0")
        val limit = Money.parse("EUR 10")
        val sponsoring = Sponsoring(team, amountPerKm, limit)

        PowerMockito.`when`(team.getLinearDistance()).thenReturn(20000.0)

        assertTrue(sponsoring.reachedLimit())
    }
}

