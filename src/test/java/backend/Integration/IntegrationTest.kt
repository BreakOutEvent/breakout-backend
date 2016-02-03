package backend.Integration


import backend.configuration.AuthorizationServerConfiguration
import backend.configuration.ResourceServerConfiguration
import backend.TestBackendConfiguration
import backend.configuration.Initializer
import backend.configuration.WebSecurityConfiguration
import backend.controller.RequestBodies.PostUserBody
import backend.model.event.EventRepository
import backend.model.event.TeamRepository
import backend.model.user.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Before
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.IntegrationTest
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.util.*
import javax.servlet.Filter
import kotlin.collections.mapOf
import kotlin.text.toByteArray

@RunWith(SpringJUnit4ClassRunner::class)
@SpringApplicationConfiguration(classes = arrayOf(TestBackendConfiguration::class, WebSecurityConfiguration::class,
        ResourceServerConfiguration::class, AuthorizationServerConfiguration::class, Initializer::class))
@WebAppConfiguration
@IntegrationTest("server.port:0")
abstract class IntegrationTest {


    @Autowired lateinit private var context: WebApplicationContext
    @Autowired lateinit protected var userRepository: UserRepository
    @Autowired lateinit protected var eventRepository: EventRepository
    @Autowired lateinit protected var springSecurityFilterChain: Filter
    @Autowired lateinit protected var teamRepository: TeamRepository

    lateinit protected var mockMvc: MockMvc

    companion object {

        var counter = 0;

        fun getDummyPostUserBody(): PostUserBody {
            val body = PostUserBody().apply {
                email = "nr$counter@icloud.com"
                firstname = "Florian"
                lastname = "Schmidt"
                gender = "Male"
                password = "Awesome password"
            }
            counter++
            return body
        }
    }

    @Before
    open fun setUp() {
        teamRepository.deleteAll()
        userRepository.deleteAll()
        eventRepository.deleteAll()
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilters<DefaultMockMvcBuilder>(springSecurityFilterChain)
                .build()
    }

    fun post(path: String, json: String): MockHttpServletRequestBuilder {
        return MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON_VALUE).content(json)
    }

    fun put(path: String, json: String): MockHttpServletRequestBuilder {
        return MockMvcRequestBuilders.put(path).contentType(MediaType.APPLICATION_JSON_VALUE).content(json)
    }

    operator fun get(path: String): MockHttpServletRequestBuilder {
        return MockMvcRequestBuilders.get(path)
    }


}

// Add .toJsonString() to class map
fun Map<String, kotlin.Any>.toJsonString() = ObjectMapper().writeValueAsString(this)

// Create a user via the API and return it's credentials
fun createUser(mockMvc: MockMvc, email: String = "a@x.de", password: String = "password"): Credentials {

    // Create user
    val userdata = mapOf(
            "email" to email,
            "password" to password
    ).toJsonString()

    val createRequest = MockMvcRequestBuilders
            .request(HttpMethod.POST, "/user/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(userdata)

    val createResponseString = mockMvc.perform(createRequest)
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
            .andReturn().response.contentAsString

    val createResponse: Map<String, kotlin.Any> = ObjectMapper()
            .reader(Map::class.java)
            .readValue(createResponseString)

    val id = createResponse["id"] as Int

    val accessToken = getTokens(mockMvc, email, password).first
    val refreshToken = getTokens(mockMvc, email, password).second

    return Credentials(id, accessToken, refreshToken)
}

fun getTokens(mockMvc: MockMvc, email: String, password: String): Pair<String, String> {
    // Get credentials for created user
    val clientCredentials = Base64.getEncoder().encodeToString("breakout_app:123456789".toByteArray())

    val request = MockMvcRequestBuilders
            .post("/oauth/token")
            .param("password", password)
            .param("username", email)
            .param("scope", "read write")
            .param("grant_type", "password")
            .param("client_secret", "123456789")
            .param("client_id", "breakout_app")
            .header("Authorization", "Basic $clientCredentials")
            .accept(MediaType.APPLICATION_JSON_VALUE)

    val oauthResponseString = mockMvc.perform(request).andReturn().response.contentAsString
    val oauthResponse: Map<String, Any> = ObjectMapper()
            .reader(Map::class.java)
            .readValue(oauthResponseString)

    val accessToken = oauthResponse["access_token"] as String
    val refreshToken = oauthResponse["refresh_token"] as String
    return Pair(accessToken, refreshToken)
}

class Credentials(val id: Int, val accessToken: String, val refreshToken: String)


