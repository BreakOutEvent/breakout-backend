package backend.model.user

import org.junit.Before
import org.junit.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserCoreTest {

    lateinit var userCore: UserCore

    @Before
    fun setUp() {
        userCore = UserCore()
        userCore.email = "florian.schmidt.1994@icloud.com"
        userCore.passwordHash = BCryptPasswordEncoder().encode("password")
    }

    @Test
    fun testActivate() {
        val token = userCore.createActivationToken()
        userCore.activate(token)
        assertTrue(userCore.isActivated())
    }

    @Test
    fun testIsActivationTokenCorrect() {
        val token = userCore.createActivationToken()
        assertTrue(userCore.isActivationTokenCorrect(token))
    }

    @Test
    fun testCreateActivationToken() {
        val token = userCore.createActivationToken()
        assertEquals(token, userCore.getActivationToken())
    }

    @Test
    fun testIsActivated() {
        assertFalse(userCore.isActivated())
    }
}
