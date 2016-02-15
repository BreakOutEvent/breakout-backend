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
        userCore.addRole(Participant::class.java)
        assertFails { userCore.addRole(Participant::class.java) }
    }

    @Test
    fun testGetRole() {
        userCore.addRole(Participant::class.java)
        val role = userCore.getRole(Participant::class.java)
        assertNotNull(role)
    }

    @Test
    fun testHasRole() {
        userCore.addRole(Participant::class.java)
        assertTrue { userCore.hasRole(Participant::class.java) }
    }

    @Test
    fun testRemoveRole() {
        userCore.addRole(Participant::class.java)
        userCore.removeRole(Participant::class.java)
        assertFalse { userCore.hasRole(Participant::class.java) }
        assertNull(userCore.getRole(Participant::class.java))
    }

    @Test
    fun testGetCore() {
        val core = userCore.core
        assertEquals(userCore.core, core)
    }
}
