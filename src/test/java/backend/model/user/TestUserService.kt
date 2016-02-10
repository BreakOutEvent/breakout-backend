package backend.model.user

import backend.Integration.IntegrationTest
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import kotlin.test.*

class TestUserService : IntegrationTest() {

    @Autowired
    private lateinit var userService: UserService

    @Test
    fun create() {
        val body = getDummyPostUserBody()
        assertNotNull(userService.create(body))
        assertFailsWith(Exception::class.java, { userService.create(body) })
    }

    @Test
    fun getUserById() {
        val body = getDummyPostUserBody()
        val user = userService.create(body)
        val user2 = userService.getUserById(user!!.core!!.id!!)
        assertEquals(user.core!!.id, user2!!.core!!.id)
        assertTrue(BCryptPasswordEncoder().matches("Awesome password", user.passwordHash))
        assertFalse(BCryptPasswordEncoder().matches("Not Awesome password", user.passwordHash))
    }

    @Test
    fun getUserByEmail() {
        val body = getDummyPostUserBody()
        val user = userService.create(body)
        val user2 = userService.getUserByEmail(user!!.email)
        assertEquals(user.core!!.email, user2!!.core!!.email)
    }

    @Test
    fun getAllUsers() {
        val body = getDummyPostUserBody()
        userService.create(body)

        body.email = "florian@mail.de"
        userService.create(body)

        assertNotNull(userService.getAllUsers())
        assertEquals(userService.getAllUsers()!!.count(), 2)
    }

    @Test
    fun existsEmail() {
        val body = getDummyPostUserBody()
        userService.create(body)

        body.email = "florian@mail.de"
        userService.create(body)

        assertTrue(userService.exists(body.email!!))
        assertFalse(userService.exists("f@s.com"))
    }

    @Test
    fun saveWithRoles() {
        val user = userService.create("a@b.c", "pw")
        user.addRole(Participant::class.java)
        userService.save(user)

        val foundUser = userService.getAllUsers()!!.first()
        val foundParticipant = foundUser.getRole(Participant::class.java) as Participant

        assertTrue(foundUser.hasRole(Participant::class.java))
        assertNotNull(foundParticipant.id)
    }

}

