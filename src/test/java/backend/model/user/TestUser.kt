package backend.model.user

import backend.TestBackendConfiguration
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.IntegrationTest
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration

@RunWith(SpringJUnit4ClassRunner::class)
@SpringApplicationConfiguration(classes = arrayOf(TestBackendConfiguration::class))
@WebAppConfiguration
@IntegrationTest("server.port:0")
class TestUser {

    @Autowired
    private val userRepository: UserRepository? = null

    @Before
    fun setUp() {
        userRepository!!.deleteAll()
    }

    /**
     * Create new user without any roles
     */
    @Test
    fun createUserWithoutRole() {
        val user = UserCore()
        assertFalse(user.hasRole(Employee::class.java))
        assertNull(user.getRole(Employee::class.java))
        assertNull(user.removeRole(Employee::class.java))
    }

    /**
     * Create new user with role employee

     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun createUserWithRoleEmployee() {

        val user = UserCore()
        user.addRole(Employee::class.java)

        assertTrue(user.hasRole(Employee::class.java))

        val emp = user.getRole(Employee::class.java) as Employee?

        assertNotNull(emp)
        assertEquals(emp, user.removeRole(Employee::class.java))
        assertNull(user.removeRole(Employee::class.java))
    }

    /**
     * Create user and add some details
     * Check if role delegates calls to core

     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun createUserWithDetails() {
        val user = UserCore()
        user.firstname = "Florian"
        user.lastname = "Schmidt"
        user.email = "florian.schmidt.1994@icloud.com"
        user.isBlocked = false
        user.passwordHash = "Lorem ipsum"
        user.gender = "Male"

        val emp = user.addRole(Employee::class.java) as Employee

        // Check user
        assertEquals("Florian", user.firstname)
        assertEquals("Schmidt", user.lastname)
        assertEquals("florian.schmidt.1994@icloud.com", user.email)
        assertEquals(false, user.isBlocked)
        assertEquals("Lorem ipsum", user.passwordHash)
        assertEquals("Male", user.gender)

        // Check it's roles getter
        assertEquals("Florian", emp.firstname)
        assertEquals("Schmidt", emp.lastname)
        assertEquals("florian.schmidt.1994@icloud.com", emp.email)
        assertEquals(false, emp.isBlocked)
        assertEquals("Lorem ipsum", user.passwordHash)
        assertEquals("Male", emp.gender)

        // Check it's roles setter
        emp.firstname = "_Florian"
        emp.lastname = "_Schmidt"
        emp.email = "_florian.schmidt.1994@icloud.com"
        emp.isBlocked = true
        emp.passwordHash = "_Lorem ipsum"
        emp.gender = "_Male"

        assertEquals("_Florian", user.firstname)
        assertEquals("_Schmidt", user.lastname)
        assertEquals("_florian.schmidt.1994@icloud.com", user.email)
        assertEquals(true, user.isBlocked)
        assertEquals("_Lorem ipsum", user.passwordHash)
        assertEquals("_Male", user.gender)
    }

    @Test
    @Throws(Exception::class)
    fun createAndSaveUser() {

        // Create user with role and save it
        val user = UserCore().addRole(Employee::class.java).apply {
            firstname = "Florian"
            lastname = "Schmidt"
            email = "florian.schmidt.1995@icloud.com"
            isBlocked = false
            passwordHash = "Lorem ipsum"
            gender = "Male"
        }

        userRepository!!.save(user.core)

        // Check if saved user can be found again
        val user1 = userRepository.findByEmail("florian.schmidt.1995@icloud.com")
        assertNotNull(user.core!!)
        assertEquals(user.core!!.id, user1.core!!.id)
        assertTrue(user.hasRole(Employee::class.java))

        // Add and remove roles from user and save
        user1.addRole(Participant::class.java)
        user1.removeRole(Employee::class.java)
        userRepository.save(user1.core)

        // Check if found user has correct roles
        val user2 = userRepository.findByEmail("florian.schmidt.1995@icloud.com")
        assertEquals(user.core!!.id, user2.core!!.id)
        assertTrue(user2.hasRole(Participant::class.java))
        assertFalse(user2.hasRole(Employee::class.java))

    }
}
