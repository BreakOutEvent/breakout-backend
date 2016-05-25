package backend.services

import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(PowerMockRunner::class)
@PrepareForTest(FeatureRepository::class)
class FeatureFlagServiceImplTest {

    private lateinit var service: FeatureFlagService
    private lateinit var repository: FeatureRepository

    @Test
    fun testIsEnabled() {

        repository = PowerMockito.mock(FeatureRepository::class.java)
        service = FeatureFlagServiceImpl(repository)
        val key = "posting"

        PowerMockito.`when`(repository.findByName(key)).thenReturn(Feature(key, true))

        assertTrue(service.isEnabled(key))
    }

    @Test
    fun testIsDisabled() {

        repository = PowerMockito.mock(FeatureRepository::class.java)
        service = FeatureFlagServiceImpl(repository)
        val key = "posting"

        PowerMockito.`when`(repository.findByName(key)).thenReturn(Feature(key, false))

        assertFalse(service.isEnabled(key))
    }

    @Test
    fun testEnabledIfKeyNotFound() {

        repository = PowerMockito.mock(FeatureRepository::class.java)
        service = FeatureFlagServiceImpl(repository)
        val key = "posting"

        PowerMockito.`when`(repository.findByName(key)).thenReturn(null)

        assertTrue(service.isEnabled(key))
    }

    @Test
    fun testEnabledIfException() {

        repository = PowerMockito.mock(FeatureRepository::class.java)
        service = FeatureFlagServiceImpl(repository)
        val key = "posting"

        PowerMockito.`when`(repository.findByName(key)).thenThrow(Exception::class.java)

        assertTrue(service.isEnabled(key))
    }
}
