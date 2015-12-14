package backend.Integration

import backend.model.user.UserService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestUserEndpoint : IntegrationTest() {

    private fun url(): String {
        return "/user/"
    }

    private fun url(id: Int): String {
        return "/user/${id.toString()}/"
    }

    @Autowired
    lateinit var userService: UserService

    @Before
    override fun setUp() {
        super.setUp()
    }

    // TODO: Restricted Access based on roles

    /**
     * POST /user/
     * Create user with email and password
     */
    @Test
    fun postUser() {

        val json = mapOf(
                "email" to "a@x.de",
                "password" to "password"
        ).toJsonString()

        mockMvc.perform(post(url(), json))
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())

        val user = userRepository.findByEmail("a@x.de")
        assertNotNull(user)
        assertEquals(user.email, "a@x.de")
    }

    /**
     * POST /user/
     * Reject invalid email
     */
    @Test
    fun postUserRejectInvalidEmail() {
        val json = mapOf(
                "email" to "asd.de",
                "password" to "password"
        ).toJsonString()

        mockMvc.perform(post(url(), json))
                .andExpect(status().isBadRequest)
                .andExpect(content().string(""))
    }

    /**
     * POST /user/
     * Reject existing email
     */
    @Test
    fun postUserRejectExistingEmail() {
        val json = mapOf(
                "email" to "a@x.de",
                "password" to "password"
        ).toJsonString()

        mockMvc.perform(post(url(), json))
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())

        mockMvc.perform(post(url(), json))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error").value("user with email a@x.de already exists"))
    }

    /**
     * PUT /user/:id/
     * Modify the data of a user
     */
    @Test
    fun putUserId() {

        // Create user
        var json = mapOf(
                "email" to "a@x.de",
                "password" to "password"
        ).toJsonString()

        val result = mockMvc.perform(post(url(), json))
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())
                .andReturn()

        val response: Map<String, kotlin.Any> = ObjectMapper()
                .reader(Map::class.java)
                .readValue(result.response.contentAsString)

        val id = response["id"] as Int

        // Update user
        json = mapOf(
                "firstname" to "Florian",
                "lastname" to "Schmidt",
                "gender" to "Male",
                "blocked" to true
        ).toJsonString()

        val res = mockMvc.perform(put(url(id), json))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.email").value("a@x.de"))
                .andExpect(jsonPath("$.firstname").value("Florian"))
                .andExpect(jsonPath("$.lastname").value("Schmidt"))
                .andExpect(jsonPath("$.gender").value("Male"))
                .andExpect(jsonPath("$.blocked").value(true))
                .andReturn()
        // TODO: Check if user is persistent in database!
        // TODO: Check that some values such as passwordHash aren't shown!
        println(res.response.contentAsString)
    }
    // TODO: Test response if user does not exist
    // TODO: Can't override existing properties with null!
}