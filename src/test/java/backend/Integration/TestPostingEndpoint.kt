@file:JvmName("TestPostingEndpoint")

package backend.Integration

import backend.model.event.Event
import backend.model.event.Team
import backend.model.media.Media
import backend.model.misc.Coord
import backend.model.user.Admin
import backend.model.user.Participant
import backend.model.user.User
import backend.services.ConfigurationService
import backend.testHelper.asUser
import backend.testHelper.json
import com.auth0.jwt.Algorithm
import com.auth0.jwt.JWTSigner
import org.hamcrest.Matchers.hasSize
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime
import java.time.ZoneOffset

open class TestPostingEndpoint : IntegrationTest() {

    @Autowired
    private lateinit var configurationService: ConfigurationService
    private lateinit var JWT_SECRET: String
    private lateinit var user: User
    private lateinit var event: Event
    private lateinit var team: Team

    @Before
    override fun setUp() {
        super.setUp()

        this.JWT_SECRET = configurationService.getRequired("org.breakout.api.jwt_secret")

        event = eventService.createEvent("Breakout München", LocalDateTime.now(), "Munich", Coord(0.0, 0.0), 36)
        userService.create("test_admin@break-out.org", "password", { addRole(Admin::class); isBlocked = false })
        user = userService.create("test@mail.com", "password", { addRole(Participant::class) })
        team = teamService.create(user.getRole(Participant::class)!!, "name", "description", event)
    }

    @Test
    open fun createNewPostingWithTextAndLocationAndMedia() {
        val postData = mapOf(
                "text" to "TestPost",
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                "postingLocation" to mapOf(
                        "latitude" to 0.0,
                        "longitude" to 0.0
                ),
                "uploadMediaTypes" to arrayOf(
                        "image",
                        "image",
                        "audio"
                )
        )

        val request = post("/posting/")
                .asUser(mockMvc, user.email, "password")
                .json(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.text").exists())
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.user").exists())
                .andExpect(jsonPath("$.postingLocation.latitude").exists())
                .andExpect(jsonPath("$.postingLocation.longitude").exists())
                .andExpect(jsonPath("$.postingLocation.date").exists())
                .andExpect(jsonPath("$.postingLocation.distance").exists())
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
    open fun createNewPostingWithText() {
        val data = mapOf(
                "text" to "TestPost",
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        )

        val request = post("/posting/")
                .asUser(mockMvc, user.email, "password")
                .json(data)

        val response = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.text").value("TestPost"))
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.user").exists())
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    open fun createNewPostingWithTextAndHashtags() {
        val postData = mapOf(
                "text" to "hello #breakout bla blub #awsome",
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        )

        val request = post("/posting/")
                .asUser(mockMvc, user.email, "password")
                .json(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.text").value("hello #breakout bla blub #awsome"))
                .andExpect(jsonPath("$.hashtags").isArray)
                .andExpect(jsonPath("$.hashtags[0]").value("breakout"))
                .andExpect(jsonPath("$.hashtags[1]").value("awsome"))
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.user").exists())
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    open fun getPostingsByHashTag() {
        val posting = postingService.savePostingWithLocationAndMedia("hello #breakout", null, user.core, null, 0.0, LocalDateTime.now())
        postingService.savePostingWithLocationAndMedia("hello #awsome", null, user.core, null, 0.0, LocalDateTime.now())

        val request = MockMvcRequestBuilders
                .get("/posting/hashtag/breakout/")

        val response = mockMvc.perform (request)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].text").exists())
                .andExpect(jsonPath("$[0].date").exists())
                .andExpect(jsonPath("$[0].user").exists())
                .andExpect(jsonPath("$[0].text").value("hello #breakout"))
                .andExpect(jsonPath("$[0].hashtags").isArray)
                .andExpect(jsonPath("$[0].hashtags[0]").value("breakout"))
                .andExpect(jsonPath("$[1]").doesNotExist())
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    open fun createNewPostingWithMedia() {
        val postData = mapOf(
                "uploadMediaTypes" to arrayOf(
                        "image",
                        "image",
                        "audio"
                ),
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        )

        val request = post("/posting/")
                .asUser(mockMvc, user.email, "password")
                .json(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
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
    open fun createNewPostingWithLocation() {
        val postData = mapOf(
                "postingLocation" to mapOf(
                        "latitude" to 0.0,
                        "longitude" to 0.0
                ),
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        )

        val request = post("/posting/")
                .asUser(mockMvc, user.email, "password")
                .json(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.postingLocation.latitude").exists())
                .andExpect(jsonPath("$.postingLocation.longitude").exists())
                .andExpect(jsonPath("$.postingLocation.date").exists())
                .andExpect(jsonPath("$.postingLocation.distance").exists())
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.user").exists())
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    open fun dontCreatePostingForInvalidJSON() {
        val postData = mapOf(
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        )

        val request = post("/posting/")
                .asUser(mockMvc, user.email, "password")
                .json(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isBadRequest)
                .andReturn().response.contentAsString

        println(response)
    }


    @Test
    open fun dontCreatePostingWithoutValidAuth() {
        val postData = mapOf(
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                "postingLocation" to mapOf(
                        "latitude" to 0.0,
                        "longitude" to 0.0
                )
        )

        val request = post("/posting/")
                .header("Authorization", "Bearer invalidToken")
                .json(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isUnauthorized)
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    open fun getPostingById() {
        //given
        val posting = postingService.savePostingWithLocationAndMedia("Test", Coord(0.0, 0.0), user.core, null, 0.0, LocalDateTime.now())

        //when
        val request = get("/posting/${posting.id}/")

        //then
        val response = mockMvc.perform (request)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.text").exists())
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.user").exists())
                .andExpect(jsonPath("$.postingLocation.latitude").exists())
                .andExpect(jsonPath("$.postingLocation.longitude").exists())
                .andExpect(jsonPath("$.postingLocation.date").exists())
                .andExpect(jsonPath("$.postingLocation.distance").exists())
                .andReturn().response.contentAsString

        println(response)
    }


    @Test
    open fun getPostingsByIds() {
        //given

        val postingZero = postingService.savePostingWithLocationAndMedia("Test0", Coord(0.0, 0.0), user.core, null, 0.0, LocalDateTime.now())
        postingService.savePostingWithLocationAndMedia("Test1", Coord(0.0, 0.0), user.core, null, 0.0, LocalDateTime.now())
        val postingTwo = postingService.savePostingWithLocationAndMedia("Test2", Coord(0.0, 0.0), user.core, null, 0.0, LocalDateTime.now())

        val postingsIds: List<Long> = listOf(postingZero.id!!, postingTwo.id!!)

        //when
        val request = post("/posting/get/ids")
                .json(postingsIds)

        //then
        val response = mockMvc.perform (request)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath<MutableCollection<out Any>>("$", hasSize(2)))
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[0].text").value("Test0"))
                .andExpect(jsonPath("$[1]").exists())
                .andExpect(jsonPath("$[1].text").value("Test2"))
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    open fun getPostingIdsSince() {
        //given

        val postingZero = postingService.savePostingWithLocationAndMedia("Test0", Coord(0.0, 0.0), user.core, null, 0.0, LocalDateTime.now())
        postingService.savePostingWithLocationAndMedia("Test1", Coord(0.0, 0.0), user.core, null, 0.0, LocalDateTime.now())
        postingService.savePostingWithLocationAndMedia("Test2", Coord(0.0, 0.0), user.core, null, 0.0, LocalDateTime.now())

        //when
        val request = get("/posting/get/since/${postingZero.id}/")

        //then
        val response = mockMvc.perform (request)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath<MutableCollection<out Any>>("$", hasSize(2)))
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[1]").exists())
                .andReturn().response.contentAsString

        println(response)
    }


    @Test
    open fun createNewPostingWithMediaAndAddMediaSizesWithoutToken() {
        //given

        val posting = postingService.savePostingWithLocationAndMedia("Test", Coord(0.0, 0.0), user.core, null, 0.0, LocalDateTime.now());
        val media = mediaService.createMedia("image")
        posting.media = listOf(media) as MutableList<Media>
        val savedposting = postingService.save(posting)

        val postData = mapOf(
                "url" to "https://aws.amazon.com/bla.jpg",
                "width" to 400,
                "height" to 200,
                "length" to 0.0,
                "size" to 0.0,
                "type" to "image"
        )

        //when
        val request = post("/media/${savedposting!!.media!!.first().id}/")
                .json(postData)

        //then
        val response = mockMvc.perform (request)
                .andExpect(status().isBadRequest)
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    open fun createNewPostingWithMediaAndAddMediaSizesWithWrongToken() {


        val posting = postingService.savePostingWithLocationAndMedia("Test", Coord(0.0, 0.0), user.core, null, 0.0, LocalDateTime.now());
        val media = mediaService.createMedia("image")
        posting.media = listOf(media) as MutableList<Media>
        val savedposting = postingService.save(posting)

        val postData = mapOf(
                "url" to "https://aws.amazon.com/bla.jpg",
                "width" to 400,
                "height" to 200,
                "length" to 0.0,
                "size" to 0.0,
                "type" to "image"
        )

        val request = post("/media/${savedposting!!.media!!.first().id}/")
                .header("X-UPLOAD-TOKEN", "87654321")
                .json(postData)

        val response = mockMvc.perform (request)
                .andExpect(status().isUnauthorized)
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    open fun createNewPostingWithMediaAndAddMediaSizesWithValidToken() {


        val posting = postingService.savePostingWithLocationAndMedia("Test", Coord(0.0, 0.0), user.core, null, 0.0, LocalDateTime.now());
        val media = mediaService.createMedia("image")
        posting.media = listOf(media) as MutableList<Media>
        val savedposting = postingService.save(posting)

        val postData = mapOf(
                "url" to "https://aws.amazon.com/bla.jpg",
                "width" to 400,
                "height" to 200,
                "length" to 0.0,
                "size" to 0.0,
                "type" to "image"
        )

        println(posting.media)

        val request = post("/media/${savedposting!!.media!!.first().id}/")
                .header("X-UPLOAD-TOKEN", JWTSigner(JWT_SECRET).sign(mapOf("subject" to posting.media!!.first().id.toString()), JWTSigner.Options().setAlgorithm(Algorithm.HS512)))
                .json(postData)

        val response = mockMvc.perform (request)
                .andExpect(status().isCreated)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.url").exists())
                .andExpect(jsonPath("$.width").exists())
                .andExpect(jsonPath("$.height").exists())
                .andExpect(jsonPath("$.length").exists())
                .andExpect(jsonPath("$.size").exists())
                .andExpect(jsonPath("$.type").exists())
                .andReturn().response.contentAsString


        println(response)

        val requestMedia = get("/posting/${savedposting.id}/")

        val responseMedia = mockMvc.perform (requestMedia)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.media").exists())
                .andExpect(jsonPath("$.media").isArray)
                .andExpect(jsonPath("$.media[0]").exists())
                .andExpect(jsonPath("$.media[0].id").exists())
                .andExpect(jsonPath("$.media[0].type").exists())
                .andExpect(jsonPath("$.media[0].sizes").exists())
                .andExpect(jsonPath("$.media[0].sizes").isArray)
                .andExpect(jsonPath("$.media[0].sizes[0]").exists())
                .andExpect(jsonPath("$.media[0].sizes[0].id").exists())
                .andExpect(jsonPath("$.media[0].sizes[0].url").exists())
                .andExpect(jsonPath("$.media[0].sizes[0].width").exists())
                .andExpect(jsonPath("$.media[0].sizes[0].height").exists())
                .andExpect(jsonPath("$.media[0].sizes[0].length").exists())
                .andExpect(jsonPath("$.media[0].sizes[0].size").exists())
                .andExpect(jsonPath("$.media[0].sizes[0].type").exists())
                .andReturn().response.contentAsString

        println(responseMedia)
    }

    @Test
    open fun createNewPostingWithMediaAndAddMediaSizesUpdateMediaSizes() {


        val posting = postingService.savePostingWithLocationAndMedia("Test", Coord(0.0, 0.0), user.core, null, 0.0, LocalDateTime.now());
        val media = mediaService.createMedia("image")
        posting.media = listOf(media) as MutableList<Media>
        val savedposting = postingService.save(posting)

        var postData = mapOf(
                "url" to "https://aws.amazon.com/bla.jpg",
                "width" to 400,
                "height" to 200,
                "length" to 0.0,
                "size" to 0.0,
                "type" to "image"
        )

        println(posting.media)

        var request = post("/media/${savedposting!!.media!!.first().id}/")
                .header("X-UPLOAD-TOKEN", JWTSigner(JWT_SECRET).sign(mapOf("subject" to posting.media!!.first().id.toString()), JWTSigner.Options().setAlgorithm(Algorithm.HS512)))
                .json(postData)

        var response = mockMvc.perform (request)
                .andExpect(status().isCreated)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.url").exists())
                .andExpect(jsonPath("$.width").exists())
                .andExpect(jsonPath("$.height").exists())
                .andExpect(jsonPath("$.length").exists())
                .andExpect(jsonPath("$.size").exists())
                .andExpect(jsonPath("$.type").exists())
                .andReturn().response.contentAsString


        println(response)

        postData = mapOf(
                "url" to "https://aws.amazon.com/bla123.jpg",
                "width" to 400,
                "height" to 200,
                "length" to 0.0,
                "size" to 0.0,
                "type" to "image"
        )

        println(posting.media)

        request = post("/media/${savedposting.media!!.first().id}/")
                .header("X-UPLOAD-TOKEN", JWTSigner(JWT_SECRET).sign(mapOf("subject" to posting.media!!.first().id.toString()), JWTSigner.Options().setAlgorithm(Algorithm.HS512)))
                .json(postData)

        response = mockMvc.perform (request)
                .andExpect(status().isCreated)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.url").exists())
                .andExpect(jsonPath("$.width").exists())
                .andExpect(jsonPath("$.height").exists())
                .andExpect(jsonPath("$.length").exists())
                .andExpect(jsonPath("$.size").exists())
                .andExpect(jsonPath("$.type").exists())
                .andReturn().response.contentAsString


        println(response)

        val requestMedia = MockMvcRequestBuilders
                .get("/posting/${savedposting.id}/")

        val responseMedia = mockMvc.perform (requestMedia)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.media").exists())
                .andExpect(jsonPath("$.media").isArray)
                .andExpect(jsonPath("$.media[0]").exists())
                .andExpect(jsonPath("$.media[0].id").exists())
                .andExpect(jsonPath("$.media[0].type").exists())
                .andExpect(jsonPath("$.media[0].sizes").exists())
                .andExpect(jsonPath("$.media[0].sizes").isArray)
                .andExpect(jsonPath("$.media[0].sizes[0]").exists())
                .andExpect(jsonPath("$.media[0].sizes[0].id").exists())
                .andExpect(jsonPath("$.media[0].sizes[0].url").exists())
                .andExpect(jsonPath("$.media[0].sizes[0].url").value("https://aws.amazon.com/bla123.jpg"))
                .andExpect(jsonPath("$.media[0].sizes[0].width").exists())
                .andExpect(jsonPath("$.media[0].sizes[0].height").exists())
                .andExpect(jsonPath("$.media[0].sizes[0].length").exists())
                .andExpect(jsonPath("$.media[0].sizes[0].size").exists())
                .andExpect(jsonPath("$.media[0].sizes[0].type").exists())
                .andExpect(jsonPath("$.media[1]").doesNotExist())
                .andReturn().response.contentAsString

        println(responseMedia)
    }

    @Test
    open fun createNewPostingWithMediaAndAddMediaSizesUpdateMediaSizesWithVariationWidth() {


        val posting = postingService.savePostingWithLocationAndMedia("Test", Coord(0.0, 0.0), user.core, null, 0.0, LocalDateTime.now());
        val media = mediaService.createMedia("image")
        posting.media = listOf(media) as MutableList<Media>
        val savedposting = postingService.save(posting)

        var postData = mapOf(
                "url" to "https://aws.amazon.com/bla.jpg",
                "width" to 400,
                "height" to 200,
                "length" to 0.0,
                "size" to 0.0,
                "type" to "image"
        )

        println(posting.media)

        var request = post("/media/${savedposting!!.media!!.first().id}/")
                .header("X-UPLOAD-TOKEN", JWTSigner(JWT_SECRET).sign(mapOf("subject" to posting.media!!.first().id.toString()), JWTSigner.Options().setAlgorithm(Algorithm.HS512)))
                .json(postData)

        var response = mockMvc.perform (request)
                .andExpect(status().isCreated)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.url").exists())
                .andExpect(jsonPath("$.width").exists())
                .andExpect(jsonPath("$.height").exists())
                .andExpect(jsonPath("$.length").exists())
                .andExpect(jsonPath("$.size").exists())
                .andExpect(jsonPath("$.type").exists())
                .andReturn().response.contentAsString


        println(response)

        postData = mapOf(
                "url" to "https://aws.amazon.com/bla123.jpg",
                "width" to 398,
                "height" to 200,
                "length" to 0.0,
                "size" to 0.0,
                "type" to "image"
        )

        println(posting.media)

        request = post("/media/${savedposting.media!!.first().id}/")
                .header("X-UPLOAD-TOKEN", JWTSigner(JWT_SECRET).sign(mapOf("subject" to posting.media!!.first().id.toString()), JWTSigner.Options().setAlgorithm(Algorithm.HS512)))
                .json(postData)

        response = mockMvc.perform (request)
                .andExpect(status().isCreated)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.url").exists())
                .andExpect(jsonPath("$.width").exists())
                .andExpect(jsonPath("$.height").exists())
                .andExpect(jsonPath("$.length").exists())
                .andExpect(jsonPath("$.size").exists())
                .andExpect(jsonPath("$.type").exists())
                .andReturn().response.contentAsString


        println(response)

        val requestMedia = get("/posting/${savedposting.id}/")

        val responseMedia = mockMvc.perform (requestMedia)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.media").exists())
                .andExpect(jsonPath("$.media").isArray)
                .andExpect(jsonPath("$.media[0]").exists())
                .andExpect(jsonPath("$.media[0].id").exists())
                .andExpect(jsonPath("$.media[0].type").exists())
                .andExpect(jsonPath("$.media[0].sizes").exists())
                .andExpect(jsonPath("$.media[0].sizes").isArray)
                .andExpect(jsonPath("$.media[0].sizes[0]").exists())
                .andExpect(jsonPath("$.media[0].sizes[0].id").exists())
                .andExpect(jsonPath("$.media[0].sizes[0].url").exists())
                .andExpect(jsonPath("$.media[0].sizes[0].url").value("https://aws.amazon.com/bla123.jpg"))
                .andExpect(jsonPath("$.media[0].sizes[0].width").exists())
                .andExpect(jsonPath("$.media[0].sizes[0].height").exists())
                .andExpect(jsonPath("$.media[0].sizes[0].length").exists())
                .andExpect(jsonPath("$.media[0].sizes[0].size").exists())
                .andExpect(jsonPath("$.media[0].sizes[0].type").exists())
                .andExpect(jsonPath("$.media[1]").doesNotExist())
                .andReturn().response.contentAsString

        println(responseMedia)
    }

    @Test
    open fun createNewPostingWithMediaAndAddMediaSizesUpdateMediaSizesWithVariationHeight() {


        val posting = postingService.savePostingWithLocationAndMedia("Test", Coord(0.0, 0.0), user.core, null, 0.0, LocalDateTime.now());
        val media = mediaService.createMedia("image")
        posting.media = listOf(media) as MutableList<Media>
        val savedposting = postingService.save(posting)

        var postData = mapOf(
                "url" to "https://aws.amazon.com/bla.jpg",
                "width" to 200,
                "height" to 400,
                "length" to 0.0,
                "size" to 0.0,
                "type" to "image"
        )

        println(posting.media)

        var request = post("/media/${savedposting!!.media!!.first().id}/")
                .header("X-UPLOAD-TOKEN", JWTSigner(JWT_SECRET).sign(mapOf("subject" to posting.media!!.first().id.toString()), JWTSigner.Options().setAlgorithm(Algorithm.HS512)))
                .json(postData)

        var response = mockMvc.perform (request)
                .andExpect(status().isCreated)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.url").exists())
                .andExpect(jsonPath("$.width").exists())
                .andExpect(jsonPath("$.height").exists())
                .andExpect(jsonPath("$.length").exists())
                .andExpect(jsonPath("$.size").exists())
                .andExpect(jsonPath("$.type").exists())
                .andReturn().response.contentAsString


        println(response)

        postData = mapOf(
                "url" to "https://aws.amazon.com/bla123.jpg",
                "width" to 200,
                "height" to 402,
                "length" to 0.0,
                "size" to 0.0,
                "type" to "image"
        )

        println(posting.media)

        request = post("/media/${savedposting.media!!.first().id}/")
                .header("X-UPLOAD-TOKEN", JWTSigner(JWT_SECRET).sign(mapOf("subject" to posting.media!!.first().id.toString()), JWTSigner.Options().setAlgorithm(Algorithm.HS512)))
                .json(postData)

        response = mockMvc.perform (request)
                .andExpect(status().isCreated)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.url").exists())
                .andExpect(jsonPath("$.width").exists())
                .andExpect(jsonPath("$.height").exists())
                .andExpect(jsonPath("$.length").exists())
                .andExpect(jsonPath("$.size").exists())
                .andExpect(jsonPath("$.type").exists())
                .andReturn().response.contentAsString


        println(response)

        val requestMedia = get("/posting/${savedposting.id}/")

        val responseMedia = mockMvc.perform (requestMedia)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.media").exists())
                .andExpect(jsonPath("$.media").isArray)
                .andExpect(jsonPath("$.media[0]").exists())
                .andExpect(jsonPath("$.media[0].id").exists())
                .andExpect(jsonPath("$.media[0].type").exists())
                .andExpect(jsonPath("$.media[0].sizes").exists())
                .andExpect(jsonPath("$.media[0].sizes").isArray)
                .andExpect(jsonPath("$.media[0].sizes[0]").exists())
                .andExpect(jsonPath("$.media[0].sizes[0].id").exists())
                .andExpect(jsonPath("$.media[0].sizes[0].url").exists())
                .andExpect(jsonPath("$.media[0].sizes[0].url").value("https://aws.amazon.com/bla123.jpg"))
                .andExpect(jsonPath("$.media[0].sizes[0].width").exists())
                .andExpect(jsonPath("$.media[0].sizes[0].height").exists())
                .andExpect(jsonPath("$.media[0].sizes[0].length").exists())
                .andExpect(jsonPath("$.media[0].sizes[0].size").exists())
                .andExpect(jsonPath("$.media[0].sizes[0].type").exists())
                .andExpect(jsonPath("$.media[1]").doesNotExist())
                .andReturn().response.contentAsString

        println(responseMedia)
    }

    @Test
    open fun getAllPostings() {

        postingService.savePostingWithLocationAndMedia("Test", Coord(0.0, 0.0), user.core, null, 0.0, LocalDateTime.now())
        postingService.savePostingWithLocationAndMedia("Test 2", Coord(0.0, 0.0), user.core, null, 0.0, LocalDateTime.now())

        val request = get("/posting/")

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[1]").exists())
                .andReturn().response.contentAsString
    }


    @Test
    open fun createNewComment() {

        val posting = postingService.savePostingWithLocationAndMedia("Test", null, user.core, null, 0.0, LocalDateTime.now())

        val postData = mapOf(
                "text" to "TestComment",
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        )

        val request = post("/posting/${posting.id}/comment/")
                .asUser(mockMvc, user.email, "password")
                .json(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.text").value("TestComment"))
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.user").exists())
                .andReturn().response.contentAsString

        println(response)


        val requestPosting = get("/posting/${posting.id}/")

        val responsePosting = mockMvc.perform (requestPosting)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.text").exists())
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.user").exists())
                .andExpect(jsonPath("$.comments").isArray)
                .andExpect(jsonPath("$.comments[0].text").value("TestComment"))
                .andReturn().response.contentAsString

        println(responsePosting)
    }

    @Test
    open fun createNewLike() {

        val posting = postingService.savePostingWithLocationAndMedia("Test", null, user.core, null, 0.0, LocalDateTime.now())

        val postData = mapOf(
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        )

        val request = post("/posting/${posting.id}/like/")
                .asUser(mockMvc, user.email, "password")
                .json(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.user").exists())
                .andReturn().response.contentAsString

        println(response)


        val requestPosting = get("/posting/${posting.id}/")

        val responsePosting = mockMvc.perform (requestPosting)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.text").exists())
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.user").exists())
                .andExpect(jsonPath("$.likes").exists())
                .andExpect(jsonPath("$.likes").value(1))
                .andExpect(jsonPath("$.hasLiked").value(false))
                .andReturn().response.contentAsString

        println(responsePosting)
    }

    @Test
    open fun createLikeHasLikedFlag() {

        val posting = postingService.savePostingWithLocationAndMedia("Test", null, user.core, null, 0.0, LocalDateTime.now())

        val postData = mapOf(
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        )

        val request = post("/posting/${posting.id}/like/")
                .asUser(mockMvc, user.email, "password")
                .json(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.user").exists())
                .andReturn().response.contentAsString

        println(response)


        val requestPosting = get("/posting/${posting.id}/")
                .asUser(mockMvc, user.email, "password")

        val responsePosting = mockMvc.perform (requestPosting)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.text").exists())
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.user").exists())
                .andExpect(jsonPath("$.likes").exists())
                .andExpect(jsonPath("$.likes").value(1))
                .andExpect(jsonPath("$.hasLiked").value(true))
                .andReturn().response.contentAsString

        println(responsePosting)
    }

    @Test
    open fun createNewLikeFailDuplicate() {

        val posting = postingService.savePostingWithLocationAndMedia("Test", null, user.core, null, 0.0, LocalDateTime.now())

        val postData = mapOf(
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        )

        val request = post("/posting/${posting.id}/like/")
                .asUser(mockMvc, user.email, "password")
                .json(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.user").exists())
                .andReturn().response.contentAsString

        println(response)


        val requestSecond = post("/posting/${posting.id}/like/")
                .asUser(mockMvc, user.email, "password")
                .json(postData)

        val responseSecond = mockMvc.perform (requestSecond)
                .andExpect(status().isConflict)
                .andReturn().response.contentAsString

        println(responseSecond)
    }

    @Test
    open fun getLikesForPosting() {
        val posting = postingService.savePostingWithLocationAndMedia("Test", null, user.core, null, 0.0, LocalDateTime.now())
        likeService.createLike(LocalDateTime.now(), posting, user.core)

        val request = get("/posting/${posting.id}/like/")
                .asUser(mockMvc, user.email, "password")

        val response = mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].date").exists())
                .andExpect(jsonPath("$[0].user").exists())
                .andReturn().response.contentAsString

        println(response)
    }
}
