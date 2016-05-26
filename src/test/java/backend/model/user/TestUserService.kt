package backend.model.user

import backend.Integration.IntegrationTest
import org.junit.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import kotlin.test.*

class TestUserService : IntegrationTest() {

    @Test
    fun create() {
        assertNotNull(userService.create("test@mail.com", "password"))
        assertFailsWith(Exception::class, { userService.create("test@mail.com", "password") })
    }

    @Test
    fun getUserById() {
        val user = userService.create("test@mail.de", "Awesome password")
        val user2 = userService.getUserById(user.core.id!!)

        assertEquals(user.core.id, user2!!.core.id)
        assertTrue(BCryptPasswordEncoder().matches("Awesome password", user.passwordHash))
        assertFalse(BCryptPasswordEncoder().matches("Not Awesome password", user.passwordHash))
    }

    @Test
    fun getUserByEmail() {
        val user = userService.create("test@mail.de", "password")
        val user2 = userService.getUserByEmail(user.email)

        assertEquals(user.core.email, user2!!.core.email)
    }

    @Test
    fun getAllUsers() {
        userService.create("test@mail.com", "password")
        userService.create("second_test@mail.com", "password")

        assertNotNull(userService.getAllUsers())
        assertEquals(userService.getAllUsers().count(), 2)
    }

    @Test
    fun existsEmail() {
        userService.create("test@mail.com", "password")
        assertTrue(userService.exists("test@mail.com"))
        assertFalse(userService.exists("f@s.com"))
    }

    @Test
    fun saveWithRoles() {
        val user = userService.create("user@mail.com", "pw")
        user.addRole(Participant::class)
        userService.save(user)

        val foundUser = userService.getAllUsers().first()
        val foundParticipant = foundUser.getRole(Participant::class)

        assertNotNull(foundParticipant)
        assertTrue(foundUser.hasRole(Participant::class))
        assertNotNull(foundParticipant!!.id)
    }

}

