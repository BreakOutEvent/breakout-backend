package backend.model.user

import backend.Integration.IntegrationTest
import backend.controller.RequestBodies.PostUserBody
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import kotlin.test.*

class TestUserService : IntegrationTest() {

    @Autowired
    private lateinit var userService: UserService

    @Test
    fun create() {
        val body = getDummyBody()
        assertNotNull(userService.create(body))
        assertFailsWith(Exception::class.java, { userService.create(body)})
    }

    @Test
    fun getUserById() {
        val body = getDummyBody()
        val user = userService.create(body)
        val user2 = userService.getUserById(user!!.core!!.id!!)
        assertEquals(user.core!!.id, user2!!.core!!.id)
        assertTrue(BCryptPasswordEncoder().matches("Awesome password", user.passwordHash))
        assertFalse(BCryptPasswordEncoder().matches("Not Awesome password", user.passwordHash))
    }

    @Test
    fun getUserByEmail() {
        val body = getDummyBody()
        val user = userService.create(body)
        val user2 = userService.getUserByEmail(user!!.email)
        assertEquals(user.core!!.email, user2!!.core!!.email)
    }

    @Test
    fun getAllUsers() {
        val body = getDummyBody()
        userService.create(body)

        body.email = "florian@mail.de"
        userService.create(body)

        assertNotNull(userService.getAllUsers())
        assertEquals(userService.getAllUsers()!!.count(), 2)
    }

    @Test
    fun existsEmail() {
        val body = getDummyBody()
        userService.create(body)

        body.email = "florian@mail.de"
        userService.create(body)

        assertTrue(userService.exists(body.email!!))
        assertFalse(userService.exists("f@s.com"))
    }

    private fun getDummyBody(): PostUserBody {
        val body = PostUserBody()
        body.email = "florian.schmidt.1994@icloud.com"
        body.firstname = "Florian"
        body.lastname = "Schmidt"
        body.gender = "Male"
        body.password = "Awesome password"
        return body
    }

}

