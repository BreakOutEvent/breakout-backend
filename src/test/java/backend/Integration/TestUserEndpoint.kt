package backend.Integration

import backend.model.user.UserService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.fail

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

}