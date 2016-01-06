package backend.Integration

import backend.model.user.UserService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import kotlin.collections.mapOf
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestUserEndpoint : IntegrationTest() {

    private fun url(): String = "/user/"

    private fun url(id: Int): String = "/user/${id.toString()}/"

    @Autowired
    lateinit var userService: UserService

    @Before
    override fun setUp() = super.setUp() // this will delete all users from the test database

    // TODO: Restricted Access based on roles

    /**
     * GET /user/
     */
    @Test
    fun getUser() {
        userService.create(getDummyPostUserBody())
        userService.create(getDummyPostUserBody())
        mockMvc.perform(get("/user/")).andExpect {
            status().isOk
            jsonPath("$").isArray
            jsonPath("$[0].firstname").exists()
            jsonPath("$[0].lastname").exists()
            jsonPath("$[0].email").exists()
            jsonPath("$[0].gender").exists()
            jsonPath("$[0].userRoles").exists()
            jsonPath("$[0].passwordHash").exists() // TODO: Change this!!
            jsonPath("$[1].firstname").exists()
            jsonPath("$[1].lastname").exists()
            jsonPath("$[1].email").exists()
            jsonPath("$[1].gender").exists()
            jsonPath("$[1].userRoles").exists()
            jsonPath("$[1].passwordHash").exists()
        }
    }

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

        mockMvc.perform(post(url(), json)).andExpect {
            status().isCreated
            jsonPath("$.id").exists()
        }

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

        mockMvc.perform(post(url(), json)).andExpect {
            status().isBadRequest
            content().string("")
        }
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

        mockMvc.perform(post(url(), json)).andExpect {
            status().isCreated
            jsonPath("$.id").exists()
        }


        mockMvc.perform(post(url(), json)).andExpect {
            status().isBadRequest
            jsonPath("$.error").exists()
            jsonPath("$.error").value("user with email a@x.de already exists")
        }

    }

    /**
     * PUT /user/:id/
     * Modify the data of a user
     */
    @Test
    fun putUserId() {

        val id = createUser()

        // Update user
        val json = mapOf(
                "firstname" to "Florian",
                "lastname" to "Schmidt",
                "gender" to "Male",
                "blocked" to true
        ).toJsonString()

        val res = mockMvc.perform(put(url(id), json)).andExpect {
            status().isOk
            jsonPath("$.id").value(id)
            jsonPath("$.email").value("a@x.de")
            jsonPath("$.firstname").value("Florian")
            jsonPath("$.lastname").value("Schmidt")
            jsonPath("$.gender").value("Male")
            jsonPath("$.blocked").value(true)
        }.andReturn()

        // TODO: Check if user is persistent in database!
        // TODO: Check that some values such as passwordHash aren't shown!
        // TODO: Test response if user does not exist
        // TODO: Can't override existing properties with null!
        println(res.response.contentAsString)
    }

    @Test
    fun makeUserParticipant() {

        val id = createUser()

        // Update user with role participant
        val json = mapOf(
                "firstname" to "Florian",
                "lastname" to "Schmidt",
                "gender" to "Male",
                "blocked" to false,
                "participant" to mapOf(
                        "tshirtsize" to "XL",
                        "hometown" to "Dresden",
                        "phonenumber" to "01234567890",
                        "emergencynumber" to "0987654321"
                )
        ).toJsonString()

        val content = mockMvc.perform(put(url(id), json))
                .andExpect(status().isOk)
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.email").value("a@x.de"))
                .andExpect(jsonPath("$.firstname").value("Florian"))
                .andExpect(jsonPath("$.lastname").value("Schmidt"))
                .andExpect(jsonPath("$.gender").value("Male"))
                .andExpect(jsonPath("$.blocked").value(false))
                .andExpect(jsonPath("$.participant").exists())
                .andExpect(jsonPath("$.participant.tshirtsize").value("XL"))
                .andExpect(jsonPath("$.participant.hometown").value("Dresden"))
                .andExpect(jsonPath("$.participant.phonenumber").value("01234567890"))
                .andExpect(jsonPath("$.participant.emergencynumber").value("0987654321"))
                .andReturn().response.contentAsString

        println(content)
        // TODO: Check that this can only be done when the authorized user matches the user of the request
    }

    @Test
    fun getUserId() {

        // Create user
        var json = mapOf(
                "email" to "a@x.de",
                "password" to "password",
                "firstname" to "Florian",
                "lastname" to "Schmidt"
        ).toJsonString()

        val resultPut = mockMvc.perform(post(url(), json)).andExpect {
            status().isCreated
            jsonPath("$.id").exists()
        }.andReturn()

        val response: Map<String, kotlin.Any> = ObjectMapper()
                .reader(Map::class.java)
                .readValue(resultPut.response.contentAsString)

        val id = response["id"] as Int

        val responseGet = mockMvc.perform(get(url(id))).andExpect {
            status().isOk
            jsonPath("$.id").exists()
            jsonPath("$.email").exists()
            jsonPath("$.passwordHash").exists()
            jsonPath("$.firstname").exists()
            jsonPath("$.lastname").exists()
            jsonPath("$.id").value(id)
            jsonPath("$.email").value("a@x.de")
            jsonPath("$.firstname").value("Florian")
            jsonPath("$.lastname").value("Schmidt")
        }
    }

    private fun createUser(): Int {
        // Create user
        var json = mapOf(
                "email" to "a@x.de",
                "password" to "password"
        ).toJsonString()

        val result = mockMvc.perform(post(url(), json)).andExpect {
            status().isCreated
            jsonPath("$.id").exists()
        }.andReturn()

        val response: Map<String, kotlin.Any> = ObjectMapper()
                .reader(Map::class.java)
                .readValue(result.response.contentAsString)

        return response["id"] as Int
    }

}
