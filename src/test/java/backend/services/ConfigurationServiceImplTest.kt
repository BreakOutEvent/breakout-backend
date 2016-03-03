package backend.services

import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.springframework.core.env.Environment
import kotlin.test.assertEquals
import kotlin.test.assertFails

class ConfigurationServiceImplTest {

    private lateinit var configurationService: ConfigurationService
    private lateinit var environment: Environment
    private lateinit var systemWrapper: SystemWrapper
    private val propertyKey = "org.breakout.test.key"
    private val envKey = "BREAKOUT_TEST_KEY"
    private val value = "test"

    @Before
    fun setUp() {
        environment = Mockito.mock(Environment::class.java)
        systemWrapper = Mockito.mock(SystemWrapper::class.java)
        configurationService = ConfigurationServiceImpl(environment, systemWrapper)
    }

    @Test
    fun testGetFromProperty() {
        Mockito.`when`(environment.getProperty(propertyKey)).thenReturn(value)
        Mockito.`when`(systemWrapper.getenv(envKey)).thenReturn(null)
        assertEquals(value, configurationService.get(propertyKey))
    }

    @Test
    fun testGetFromEnvironemt() {
        Mockito.`when`(environment.getProperty(propertyKey)).thenReturn(null)
        Mockito.`when`(systemWrapper.getenv(envKey)).thenReturn(value)

        assertEquals(value, configurationService.get(propertyKey))
    }

    @Test
    fun testGetRequired() {
        Mockito.`when`(environment.getProperty(propertyKey)).thenReturn(null)
        Mockito.`when`(systemWrapper.getenv(envKey)).thenReturn(null)

        assertFails { configurationService.getRequired(propertyKey) }
    }
}
