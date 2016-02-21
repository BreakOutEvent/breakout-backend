package backend.Integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Before
import org.junit.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.RequestBuilder
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestUserEndpoint : IntegrationTest() {

    private fun url(): String = "/user/"

    private fun url(id: Int): String = "/user/${id.toString()}/"

    @Before
    override fun setUp() = super.setUp()

    /**
     * GET /user/
     */
    @Test
    fun getUser() {

        userService.create("test@mail.com", "password", {
            firstname = "Florian"
            lastname = "Schmidt"
            gender = "Male"
        })

        userService.create("secondTest@mail.com", "password", {
            firstname = "Leo"
            lastname = "Theo"
            gender = "Male"
        })

        mockMvc.perform(get("/user/"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$[0].firstname").exists())
                .andExpect(jsonPath("$[0].lastname").exists())
                .andExpect(jsonPath("$[0].email").exists())
                .andExpect(jsonPath("$[0].gender").exists())
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist())
                .andExpect(jsonPath("$[1].firstname").exists())
                .andExpect(jsonPath("$[1].lastname").exists())
                .andExpect(jsonPath("$[1].email").exists())
                .andExpect(jsonPath("$[1].gender").exists())
                .andExpect(jsonPath("$[1].passwordHash").doesNotExist())
    }

    @Test
    fun getAuthenticatedUser() {
        val credentials = createUser(this.mockMvc, userService = userService)

        val request = MockMvcRequestBuilders.get("/me/")
                .header("Authorization", "Bearer ${credentials.accessToken}")

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").exists())
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
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").exists())
    }

    /**
     * POST /user/
     * Reject existing email
     */
    @Test
    fun postUserRejectExistingEmail() {
        val json = mapOf(
                "email" to "test@mail.de",
                "password" to "password"
        ).toJsonString()

        mockMvc.perform(post(url(), json))
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())

        mockMvc.perform(post(url(), json))
                .andExpect(status().isConflict)
    }

    /**
     * PUT /user/:id/
     * Modify the data of a user
     */
    @Test
    fun putUserId() {

        val credentials = createUser(this.mockMvc, userService = userService)

        // Update user
        val json = mapOf(
                "firstname" to "Florian",
                "lastname" to "Schmidt",
                "gender" to "Male",
                "blocked" to true
        ).toJsonString()

        val request = MockMvcRequestBuilders
                .put("/user/${credentials.id}/")
                .header("Authorization", "Bearer ${credentials.accessToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(credentials.id))
                .andExpect(jsonPath("$.email").value("a@x.de"))
                .andExpect(jsonPath("$.firstname").value("Florian"))
                .andExpect(jsonPath("$.lastname").value("Schmidt"))
                .andExpect(jsonPath("$.gender").value("Male"))
                .andExpect(jsonPath("$.blocked").value(false)) // A user can't block itself
                .andExpect(jsonPath("$.passwordHash").doesNotExist())

        // TODO: Can't override existing properties with null!
    }

    @Test
    fun putUserDoesNotOverrideExistingPropertiesWithNull() {
        val credentials = createUser(this.mockMvc, userService = this.userService)


        val initJson = mapOf(
                "firstname" to "Florian",
                "lastname" to "Schmidt",
                "gender" to "Male",
                "blocked" to true
        ).toJsonString()

        val initRequest = MockMvcRequestBuilders
                .put("/user/${credentials.id}/")
                .header("Authorization", "Bearer ${credentials.accessToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(initJson)

        mockMvc.perform(initRequest).andExpect(status().isOk)

        val json = mapOf(
                "firstname" to "Florian",
                "lastname" to "Schmidt",
                "gender" to "Male",
                "blocked" to true
        ).toJsonString()

        val request = MockMvcRequestBuilders
                .put("/user/${credentials.id}/")
                .header("Authorization", "Bearer ${credentials.accessToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(credentials.id))
                .andExpect(jsonPath("$.email").value("a@x.de"))
                .andExpect(jsonPath("$.firstname").value("Florian"))
                .andExpect(jsonPath("$.lastname").value("Schmidt"))
                .andExpect(jsonPath("$.gender").value("Male"))
                .andExpect(jsonPath("$.blocked").value(false)) // A user can't block itself
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
    }

    @Test
    fun putUserCanOnlyModifyItsOwnData() {
        val firstUserCredentials = createUser(this.mockMvc, "first@email.com", "pwd", this.userService)
        val secondUserCredentials = createUser(this.mockMvc, "second@email.com", "pwd", this.userService)

        val json = mapOf("firstname" to "ChangeMe").toJsonString()
        val request = MockMvcRequestBuilders
                .put("/user/${firstUserCredentials.id}/")
                .header("Authorization", "Bearer ${secondUserCredentials.accessToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        mockMvc.perform(request).andExpect(status().isUnauthorized)
    }

    @Test
    fun putUserUnauthorizedUserCantModifyUserData() {
        val json = mapOf("firstname" to "ChangeMe").toJsonString()
        val request = MockMvcRequestBuilders.put("/user/1/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        mockMvc.perform(request).andExpect(status().isUnauthorized)
    }

    @Test
    fun makeUserParticipant() {

        val credentials = createUser(this.mockMvc, userService = userService)

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

        val request = MockMvcRequestBuilders
                .put("/user/${credentials.id}/")
                .header("Authorization", "Bearer ${credentials.accessToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("id").value(credentials.id))
                .andExpect(jsonPath("$.id").value(credentials.id))
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
    }

    @Test
    fun failToMakeUserParticipantIfUnauthorized() {

        val credentials = createUser(mockMvc, userService = userService)
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

        val request = MockMvcRequestBuilders
                .put("/user/${credentials.id}/")
                .header("Authorization", "Bearer thisIsAnInvalidAccessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized)
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

        val resultPut = mockMvc.perform(post(url(), json))
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())
                .andReturn()

        val response: Map<String, kotlin.Any> = ObjectMapper()
                .reader(Map::class.java)
                .readValue(resultPut.response.contentAsString)

        val id = response["id"] as Int

        mockMvc.perform(get(url(id)))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.firstname").exists())
                .andExpect(jsonPath("$.lastname").exists())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
    }
}
