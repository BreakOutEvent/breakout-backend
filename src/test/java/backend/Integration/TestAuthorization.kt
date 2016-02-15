package backend.Integration

import org.junit.Before
import org.junit.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*

class TestAuthorization : IntegrationTest() {

    @Before
    override fun setUp() = super.setUp()

    @Test
    fun registerUserAndAuthorize() {

        // Register user by sending data to POST /user/
        val json = mapOf("email" to "test@mail.com", "password" to "random").toJsonString()
        mockMvc.perform(post("/user/", json))
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())


        // Authorize and get access and refresh token at /oauth/token
        val credentials = Base64.getEncoder().encodeToString("breakout_app:123456789".toByteArray())
        val request = MockMvcRequestBuilders
                .post("/oauth/token")
                .param("password", "random")
                .param("username", "test@mail.com")
                .param("scope", "read write")
                .param("client_secret", "123456789")
                .param("client_id", "breakout_app")
                .param("grant_type", "password")
                .header("Authorization", "Basic $credentials")
                .accept(MediaType.APPLICATION_JSON_VALUE)

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(jsonPath("$.token_type").value("bearer"))
                .andExpect(jsonPath("$.refresh_token").exists())
                .andExpect(jsonPath("$.expires_in").exists())
                .andExpect(jsonPath("$.scope").value("read write"))

        mockMvc.perform(get("/user/"))
                .andExpect(status().isOk)


        val unauthRequest = MockMvcRequestBuilders
                .post("/oauth/token")
                .param("password", "invalid_password")
                .param("username", "test@mail.com")
                .param("scope", "read write")
                .param("client_secret", "123456789")
                .param("client_id", "breakout_app")
                .header("Authorization", "Basic $credentials")
                .accept(MediaType.APPLICATION_JSON_VALUE)

        mockMvc.perform(unauthRequest)
                .andExpect(status().is4xxClientError)
    }
}
