package backend.Integration


import backend.TestBackendConfiguration
import backend.configuration.AuthorizationServerConfiguration
import backend.configuration.ResourceServerConfiguration
import backend.configuration.WebSecurityConfiguration
import backend.model.event.EventRepository
import backend.model.event.EventService
import backend.model.event.TeamRepository
import backend.model.event.TeamService
import backend.model.location.LocationRepository
import backend.model.location.LocationService
import backend.model.media.MediaRepository
import backend.model.media.MediaService
import backend.model.media.MediaSizeRepository
import backend.model.media.MediaSizeService
import backend.model.messaging.GroupMessageRepository
import backend.model.messaging.GroupMessageService
import backend.model.payment.TeamEntryFeeService
import backend.model.posting.*
import backend.model.sponsoring.SponsoringRepository
import backend.model.sponsoring.SponsoringService
import backend.model.user.User
import backend.model.user.UserRepository
import backend.model.user.UserService
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

@RunWith(SpringJUnit4ClassRunner::class)
@SpringApplicationConfiguration(classes = arrayOf(TestBackendConfiguration::class, WebSecurityConfiguration::class, ResourceServerConfiguration::class, AuthorizationServerConfiguration::class))
@WebAppConfiguration
@IntegrationTest("server.port:0")
abstract class IntegrationTest {

    protected val APPLICATION_JSON_UTF_8 = "application/json;charset=UTF-8"

    // Spring stuff
    @Autowired lateinit private var context: WebApplicationContext
    @Autowired lateinit protected var springSecurityFilterChain: Filter

    // Repositories
    @Autowired lateinit protected var userRepository: UserRepository
    @Autowired lateinit protected var eventRepository: EventRepository
    @Autowired lateinit protected var teamRepository: TeamRepository
    @Autowired lateinit protected var commentRepository: CommentRepository
    @Autowired lateinit protected var likeRepository: LikeRepository
    @Autowired lateinit protected var postingRepository: PostingRepository
    @Autowired lateinit protected var mediaRepository: MediaRepository
    @Autowired lateinit protected var mediaSizeRepository: MediaSizeRepository
    @Autowired lateinit protected var locationRepository: LocationRepository
    @Autowired lateinit protected var sponsoringRepository: SponsoringRepository
    @Autowired lateinit protected var groupMessageRepository: GroupMessageRepository

    // Services
    @Autowired lateinit protected var userService: UserService
    @Autowired lateinit protected var teamService: TeamService
    @Autowired lateinit protected var commentService: CommentService
    @Autowired lateinit protected var postingService: PostingService
    @Autowired lateinit protected var likeService: LikeService
    @Autowired lateinit protected var eventService: EventService
    @Autowired lateinit protected var mediaService: MediaService
    @Autowired lateinit protected var mediaSizeService: MediaSizeService
    @Autowired lateinit protected var teamEntryFeeService: TeamEntryFeeService
    @Autowired lateinit protected var locationService: LocationService
    @Autowired lateinit protected var sponsoringService: SponsoringService
    @Autowired lateinit protected var groupMessageService: GroupMessageService

    lateinit protected var mockMvc: MockMvc

    @Before
    open fun setUp() {
        teamRepository.deleteAll()
        likeRepository.deleteAll()
        postingRepository.deleteAll()
        commentRepository.deleteAll()
        groupMessageRepository.deleteAll()
        userRepository.deleteAll()
        mediaRepository.deleteAll()
        mediaSizeRepository.deleteAll()
        eventRepository.deleteAll()
        locationRepository.deleteAll()
        sponsoringRepository.deleteAll()
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
fun Map<String, kotlin.Any?>.toJsonString() = ObjectMapper().writeValueAsString(this)

// Add .toJsonString() to class List
fun List<kotlin.Any?>.toJsonString() = ObjectMapper().writeValueAsString(this)

// Create a user via the API and return it's credentials
fun createUser(mockMvc: MockMvc, email: String = "a@x.de", password: String = "password", userService: UserService): Credentials {

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

    val user = userService.getUserByEmail(email)!!
    user.isBlocked = false
    userService.save(user)

    val accessToken = getTokens(mockMvc, email, password).first
    val refreshToken = getTokens(mockMvc, email, password).second

    return Credentials(id, accessToken, refreshToken, user)
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

class Credentials(val id: Int, val accessToken: String, val refreshToken: String, val user: User)


