@file:JvmName("TestMediaEndpoint")

package backend.Integration

import backend.model.event.Event
import backend.model.event.Team
import backend.model.misc.Coord
import backend.model.user.Admin
import backend.model.user.Participant
import backend.model.user.User
import backend.services.ConfigurationService
import backend.testHelper.asUser
import backend.testHelper.json
import com.auth0.jwt.Algorithm
import com.auth0.jwt.JWTSigner
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

open class TestMediaEndpoint : IntegrationTest() {

    @Autowired
    private lateinit var configurationService: ConfigurationService
    private lateinit var JWT_SECRET: String
    private lateinit var user: User
    private lateinit var admin: User
    private lateinit var event: Event
    private lateinit var team: Team

    @Before
    override fun setUp() {
        super.setUp()

        this.JWT_SECRET = configurationService.getRequired("org.breakout.api.jwt_secret")

        event = eventService.createEvent("Breakout München", LocalDateTime.now(), "Munich", Coord(1.0, 1.0), 36)
        admin = userService.create("test_admin@break-out.org", "password", { addRole(Admin::class); isBlocked = false })
        user = userService.create("test@mail.com", "password", { addRole(Participant::class) })
        team = teamService.create(user.getRole(Participant::class)!!, "name", "description", event)
    }


    @Test
    open fun adminDeleteMediaSizes() {
        val posting = postingService.savePostingWithLocationAndMedia("hello #breakout", Coord(1.0, 1.0), user.account, listOf("image", "audio"), LocalDateTime.now())
        postingService.like(posting, user.account, LocalDateTime.now())
//        commentService.createComment("Hello!", LocalDateTime.now(), posting, user.account)
        postingService.addComment(posting, user.account, LocalDateTime.now(), "Hello!")

        val postData = mapOf(
                "url" to "https://aws.amazon.com/bla.jpg",
                "width" to 400,
                "height" to 200,
                "length" to 0.0,
                "size" to 0.0,
                "type" to "image"
        )

        val requestMediaSize = MockMvcRequestBuilders.post("/media/${posting.media.first().id}/")
                .header("X-UPLOAD-TOKEN", JWTSigner(JWT_SECRET).sign(mapOf("subject" to posting.media.first().id.toString()), JWTSigner.Options().setAlgorithm(Algorithm.HS512)))
                .json(postData)

        mockMvc.perform(requestMediaSize)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.url").exists())
                .andExpect(jsonPath("$.width").exists())
                .andExpect(jsonPath("$.height").exists())
                .andExpect(jsonPath("$.length").exists())
                .andExpect(jsonPath("$.size").exists())
                .andExpect(jsonPath("$.type").exists())
                .andReturn().response.contentAsString


        val requestPosting = get("/posting/${posting.id}/")

        mockMvc.perform(requestPosting)
                .andExpect(status().isOk)
                .andExpect(MockMvcResultMatchers.content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.text").exists())
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.user").exists())
                .andExpect(jsonPath("$.media").isArray)
                .andExpect(jsonPath("$.media[0].sizes[0]").exists())
                .andExpect(jsonPath("$.media[0].sizes[0].id").exists())

        val request = MockMvcRequestBuilders
                .delete("/media/${posting.media.first().id}/")
                .asUser(mockMvc, admin.email, "password")

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("success"))


        val requestPosting2 = get("/posting/${posting.id}/")

        mockMvc.perform(requestPosting2)
                .andExpect(status().isOk)
                .andExpect(MockMvcResultMatchers.content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.text").exists())
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.user").exists())
                .andExpect(jsonPath("$.media").isArray)
                .andExpect(jsonPath("$.media[0]").exists())
                .andExpect(jsonPath("$.media[0].sizes[0]").doesNotExist())
    }
}
