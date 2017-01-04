package backend.model.user

import org.junit.Before
import org.junit.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import kotlin.test.*

class UserAccountTest {

    lateinit var userAccount: UserAccount

    @Before
    fun setUp() {
        userAccount = UserAccount()
        userAccount.email = "florian.schmidt.1994@icloud.com"
        userAccount.passwordHash = BCryptPasswordEncoder().encode("password")
    }

    @Test
    fun testActivate() {
        val token = userAccount.createActivationToken()
        userAccount.activate(token)
        assertTrue(userAccount.isActivated())
    }

    @Test
    fun testIsActivationTokenCorrect() {
        val token = userAccount.createActivationToken()
        assertTrue(userAccount.isActivationTokenCorrect(token))
    }

    @Test
    fun testCreateActivationToken() {
        userAccount.createActivationToken()
    }

    @Test
    fun testIsActivated() {
        assertFalse(userAccount.isActivated())
    }

    @Test
    fun testAddRole() {
        userAccount.addRole(Participant::class)
        assertFails { userAccount.addRole(Participant::class) }
    }

    @Test
    fun testGetRole() {
        userAccount.addRole(Participant::class)
        val role = userAccount.getRole(Participant::class)
        assertNotNull(role)
    }

    @Test
    fun testHasRole() {
        userAccount.addRole(Participant::class)
        assertTrue { userAccount.hasRole(Participant::class) }
    }

    @Test
    fun testRemoveRole() {
        userAccount.addRole(Participant::class)
        userAccount.removeRole(Participant::class)
        assertFalse { userAccount.hasRole(Participant::class) }
        assertNull(userAccount.getRole(Participant::class))
    }

    @Test
    fun testGetCore() {
        val core = userAccount.account
        assertEquals(userAccount.account, core)
    }

    @Test
    fun testEquals() {
        val otherCore = UserAccount()
        otherCore.email = userAccount.email
        assertEquals(otherCore, userAccount)
        assertEquals(otherCore, otherCore)
    }
}
