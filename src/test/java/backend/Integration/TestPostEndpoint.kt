@file:JvmName("TestPostEndpoint")

package backend.Integration

import backend.model.misc.Coords
import backend.model.post.Media
import backend.model.post.MediaService
import backend.model.post.PostService
import backend.model.user.UserService
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime
import java.time.ZoneOffset

class TestPostEndpoint : IntegrationTest() {

    lateinit var userCredentials: Credentials

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var postService: PostService

    @Autowired
    lateinit var mediaService: MediaService

    @Value("\${org.breakout.api.jwt_secret}")
    private lateinit var JWT_SECRET: String

    @Before
    override fun setUp() {
        super.setUp()
        userCredentials = createUser(this.mockMvc, userService = userService)
    }

    @Test
    fun createNewPostWithTextAndLocationAndMedia() {
        val postData = mapOf(
                "text" to "TestPost",
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                "postLocation" to mapOf(
                        "latitude" to 0.0,
                        "longitude" to 0.0
                ),
                "media" to arrayOf(
                        "image",
                        "image",
                        "audio"
                )
        ).toJsonString()

        val request = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/post/")
                .header("Authorization", "Bearer ${userCredentials.accessToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.text").exists())
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.user").exists())
                .andExpect(jsonPath("$.postLocation.latitude").exists())
                .andExpect(jsonPath("$.postLocation.longitude").exists())
                .andExpect(jsonPath("$.media[0].type").exists())
                .andExpect(jsonPath("$.media[0].id").exists())
                .andExpect(jsonPath("$.media[0].uploadToken").exists())
                .andExpect(jsonPath("$.media[1].type").exists())
                .andExpect(jsonPath("$.media[1].id").exists())
                .andExpect(jsonPath("$.media[1].uploadToken").exists())
                .andExpect(jsonPath("$.media[2].type").exists())
                .andExpect(jsonPath("$.media[2].id").exists())
                .andExpect(jsonPath("$.media[2].uploadToken").exists())
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    fun createNewPostWithText() {
        val postData = mapOf(
                "text" to "TestPost",
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        ).toJsonString()

        val request = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/post/")
                .header("Authorization", "Bearer ${userCredentials.accessToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.text").exists())
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.user").exists())
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    fun createNewPostWithMedia() {
        val postData = mapOf(
                "media" to arrayOf(
                        "image",
                        "image",
                        "audio"
                ),
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        ).toJsonString()

        val request = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/post/")
                .header("Authorization", "Bearer ${userCredentials.accessToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.media[0].type").exists())
                .andExpect(jsonPath("$.media[0].id").exists())
                .andExpect(jsonPath("$.media[0].uploadToken").exists())
                .andExpect(jsonPath("$.media[1].type").exists())
                .andExpect(jsonPath("$.media[1].id").exists())
                .andExpect(jsonPath("$.media[1].uploadToken").exists())
                .andExpect(jsonPath("$.media[2].type").exists())
                .andExpect(jsonPath("$.media[2].id").exists())
                .andExpect(jsonPath("$.media[2].uploadToken").exists())
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.user").exists())
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    fun createNewPostWithLocation() {
        val postData = mapOf(
                "postLocation" to mapOf(
                        "latitude" to 0.0,
                        "longitude" to 0.0
                ),
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        ).toJsonString()

        val request = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/post/")
                .header("Authorization", "Bearer ${userCredentials.accessToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.postLocation.latitude").exists())
                .andExpect(jsonPath("$.postLocation.longitude").exists())
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.user").exists())
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    fun dontCreatePostForInvalidJSON() {
        val postData = mapOf(
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        ).toJsonString()

        val request = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/post/")
                .header("Authorization", "Bearer ${userCredentials.accessToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isBadRequest)
                .andReturn().response.contentAsString

        println(response)
    }


    @Test
    fun dontCreatePostWithoutValidAuth() {
        val postData = mapOf(
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                "postLocation" to mapOf(
                        "latitude" to 0.0,
                        "longitude" to 0.0
                )
        ).toJsonString()

        val request = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/post/")
                .header("Authorization", "Bearer invalidToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isUnauthorized)
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    fun getPostById() {
        val user = userService.create("test@mail.com", "password")
        val post = postService.createPost("Test", Coords(0.0, 0.0), user.core!!, null)

        val request = MockMvcRequestBuilders
                .request(HttpMethod.GET, "/post/" + post.id + "/")
                .contentType(MediaType.APPLICATION_JSON)

        val response = mockMvc.perform (request)
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.text").exists())
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.user").exists())
                .andExpect(jsonPath("$.postLocation.latitude").exists())
                .andExpect(jsonPath("$.postLocation.longitude").exists())
                .andReturn().response.contentAsString

        println(response)
    }


    @Test
    fun createNewPostWithMediaAndAddMediaSizesWithoutToken() {

        val user = userService.create(getDummyPostUserBody())
        val post = postService.createPost("Test", Coords(0.0, 0.0), user!!.core!!, null);
        val media = mediaService.createMedia(post, "image")
        post.media = listOf(media) as MutableList<Media>
        val savedpost = postService.save(post)

        val postData = mapOf(
                "url" to "https://aws.amazon.com/bla.jpg",
                "width" to 400,
                "height" to 200,
                "length" to 0.0,
                "size" to 0.0,
                "type" to "image"
        ).toJsonString()

        val request = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/post/media/${savedpost!!.media!!.first().id}/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(postData)

        val response = mockMvc.perform (request)
                .andExpect(status().isBadRequest)
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    fun createNewPostWithMediaAndAddMediaSizesWithWrongToken() {

        val user = userService.create(getDummyPostUserBody())
        val post = postService.createPost("Test", Coords(0.0, 0.0), user!!.core!!, null);
        val media = mediaService.createMedia(post, "image")
        post.media = listOf(media) as MutableList<Media>
        val savedpost = postService.save(post)

        val postData = mapOf(
                "url" to "https://aws.amazon.com/bla.jpg",
                "width" to 400,
                "height" to 200,
                "length" to 0.0,
                "size" to 0.0,
                "type" to "image"
        ).toJsonString()

        val request = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/post/media/${savedpost!!.media!!.first().id}/")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-UPLOAD-TOKEN", "87654321")
                .content(postData)

        val response = mockMvc.perform (request)
                .andExpect(status().isUnauthorized)
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    fun createNewPostWithMediaAndAddMediaSizesWithValidToken() {

        val user = userService.create(getDummyPostUserBody())
        val post = postService.createPost("Test", Coords(0.0, 0.0), user!!.core!!, null);
        val media = mediaService.createMedia(post, "image")
        post.media = listOf(media) as MutableList<Media>
        val savedpost = postService.save(post)

        val postData = mapOf(
                "url" to "https://aws.amazon.com/bla.jpg",
                "width" to 400,
                "height" to 200,
                "length" to 0.0,
                "size" to 0.0,
                "type" to "image"
        ).toJsonString()

        println(post.media)

        val request = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/post/media/${savedpost!!.media!!.first().id}/")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-UPLOAD-TOKEN", Jwts.builder().setSubject(savedpost.media!!.first().id.toString()).signWith(SignatureAlgorithm.HS512, JWT_SECRET).compact())
                .content(postData)

        val response = mockMvc.perform (request)
                .andExpect(status().isCreated)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.url").exists())
                .andExpect(jsonPath("$.width").exists())
                .andExpect(jsonPath("$.height").exists())
                .andExpect(jsonPath("$.length").exists())
                .andExpect(jsonPath("$.size").exists())
                .andExpect(jsonPath("$.type").exists())
                .andReturn().response.contentAsString

        println(response)
    }

}