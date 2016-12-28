package backend.model.user

import org.junit.Before
import org.junit.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import kotlin.test.*

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
        userCore.createActivationToken()
    }

    @Test
    fun testIsActivated() {
        assertFalse(userCore.isActivated())
    }

    @Test
    fun testAddRole() {
        userCore.addRole(Participant::class)
        assertFails { userCore.addRole(Participant::class) }
    }

    @Test
    fun testGetRole() {
        userCore.addRole(Participant::class)
        val role = userCore.getRole(Participant::class)
        assertNotNull(role)
    }

    @Test
    fun testHasRole() {
        userCore.addRole(Participant::class)
        assertTrue { userCore.hasRole(Participant::class) }
    }

    @Test
    fun testRemoveRole() {
        userCore.addRole(Participant::class)
        userCore.removeRole(Participant::class)
        assertFalse { userCore.hasRole(Participant::class) }
        assertNull(userCore.getRole(Participant::class))
    }

    @Test
    fun testGetCore() {
        val core = userCore.core
        assertEquals(userCore.core, core)
    }

    @Test
    fun testEquals() {
        val otherCore = UserCore()
        otherCore.email = userCore.email
        assertEquals(otherCore, userCore)
        assertEquals(otherCore, otherCore)
    }
}
