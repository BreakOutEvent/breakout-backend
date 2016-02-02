package backend.Integration


import backend.AuthorizationServerConfiguration
import backend.ResourceServerConfiguration
import backend.TestBackendConfiguration
import backend.WebSecurityConfiguration
import backend.controller.RequestBodies.PostUserBody
import backend.model.event.EventRepository
import backend.model.event.PostRepository
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
@SpringApplicationConfiguration(classes = arrayOf(TestBackendConfiguration::class, WebSecurityConfiguration::class, ResourceServerConfiguration::class, AuthorizationServerConfiguration::class))
@WebAppConfiguration
@IntegrationTest("server.port:0")
abstract class IntegrationTest {


    @Autowired lateinit private var context: WebApplicationContext
    @Autowired lateinit protected var userRepository: UserRepository
    @Autowired lateinit protected var eventRepository: EventRepository
    @Autowired lateinit protected var postRepository: PostRepository
    @Autowired lateinit protected var springSecurityFilterChain: Filter

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
        postRepository.deleteAll()
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
fun createUser(mockMvc: MockMvc): Credentials {

    // Create user
    val userdata = mapOf(
            "email" to "a@x.de",
            "password" to "password"
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

    // Get credentials for created user
    val clientCredentials = Base64.getEncoder().encodeToString("breakout_app:123456789".toByteArray())

    val request = MockMvcRequestBuilders
            .post("/oauth/token")
            .param("password", "password")
            .param("username", "a@x.de")
            .param("scope", "read write")
            .param("grant_type", "password")
            .param("client_secret", "123456789")
            .param("client_id", "breakout_app")
            .header("Authorization", "Basic $clientCredentials")
            .accept(MediaType.APPLICATION_JSON_VALUE)

    val oauthResponseString = mockMvc.perform(request).andReturn().response.contentAsString
    val oauthResponse: Map<String, kotlin.Any> = ObjectMapper()
            .reader(Map::class.java)
            .readValue(oauthResponseString)

    val accessToken = oauthResponse["access_token"] as String
    val refreshToken = oauthResponse["refresh_token"] as String

    return Credentials(id, accessToken, refreshToken)
}

class Credentials(val id: Int, val accessToken: String, val refreshToken: String)


