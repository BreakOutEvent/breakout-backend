@file:JvmName("TestPostingEndpoint")

package backend.Integration

import backend.model.event.Event
import backend.model.event.Team
import backend.model.media.Media
import backend.model.misc.Coord
import backend.model.misc.EmailAddress
import backend.model.user.Admin
import backend.model.user.Participant
import backend.model.user.User
import backend.services.ConfigurationService
import backend.testHelper.asUser
import backend.testHelper.json
import com.auth0.jwt.Algorithm
import com.auth0.jwt.JWTSigner
import com.fasterxml.jackson.databind.ObjectMapper
import org.intellij.lang.annotations.Language
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mock.web.MockHttpServletResponse
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
    open fun adminDeletePosting() {
        val posting = postingService.savePostingWithLocationAndMedia("hello breakout", null, user.account, null, LocalDateTime.now())

        val request = MockMvcRequestBuilders
                .delete("/posting/${posting.id}/")
                .asUser(mockMvc, admin.email, "password")

        val response = mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("success"))
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    open fun adminDeletePostingFailNotAdmin() {
        val posting = postingService.savePostingWithLocationAndMedia("hello breakout", null, user.account, null, LocalDateTime.now())

        val request = MockMvcRequestBuilders
                .delete("/posting/${posting.id}/")
                .asUser(mockMvc, user.email, "password")

        val response = mockMvc.perform(request)
                .andExpect(status().isForbidden)
                .andReturn().response.contentAsString

        println(response)
    }


    @Test
    open fun adminDeletePostingCascade() {
        val posting = postingService.savePostingWithLocationAndMedia("hello #breakout", Coord(1.0, 1.0), user.account, listOf("image", "audio"), LocalDateTime.now())
        postingService.like(posting, user.account, LocalDateTime.now())
        postingService.addComment(posting, user.account, LocalDateTime.now(), "Hello!")
//        commentService.createComment("Hello!", LocalDateTime.now(), posting, user.account)

        val postData = mapOf(
                "url" to "https://aws.amazon.com/bla.jpg",
                "width" to 400,
                "height" to 200,
                "length" to 0.0,
                "size" to 0.0,
                "type" to "image"
        )

        println(posting.media)

        val requestMediaSize = post("/media/${posting.media.first().id}/")
                .header("X-UPLOAD-TOKEN", JWTSigner(JWT_SECRET).sign(mapOf("subject" to posting.media.first().id.toString()), JWTSigner.Options().setAlgorithm(Algorithm.HS512)))
                .json(postData)

        mockMvc.perform(requestMediaSize)
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


        val request = MockMvcRequestBuilders
                .delete("/posting/${posting.id}/")
                .asUser(mockMvc, admin.email, "password")

        val response = mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("success"))
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    open fun adminDeleteComment() {

        val posting = postingService.savePostingWithLocationAndMedia("Test", null, user.account, null, LocalDateTime.now())
//        val comment = commentService.createComment("TestComment", LocalDateTime.now(), posting, user.account)
        postingService.addComment(posting, user.account, LocalDateTime.now(), "TestComment")
        val requestPosting = get("/posting/${posting.id}/")

        mockMvc.perform(requestPosting)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.text").exists())
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.user").exists())
                .andExpect(jsonPath("$.comments").isArray)
                .andExpect(jsonPath("$.comments[0].text").value("TestComment"))

        fun MockHttpServletResponse.asMap(): Map<String, Any> {
            val mapper = ObjectMapper()
            val body = this.contentAsString

            val json: Map<String, Any> = mapper.readValue(body, Map::class.java) as Map<String, Any>
            return json
        }

        val comments: List<Map<String, Any>> = mockMvc.perform(get("/posting/${posting.id}/"))
                .andReturn().response.asMap()["comments"]!! as List<Map<String, Any>>

        val commentId = comments.first()["id"]!!

        val requestDelete = MockMvcRequestBuilders
                .delete("/posting/${posting.id}/comment/${commentId}/")
                .asUser(mockMvc, admin.email, "password")

        mockMvc.perform(requestDelete)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("success"))


        val requestPosting2 = get("/posting/${posting.id}/")

        mockMvc.perform(requestPosting2)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.text").exists())
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.user").exists())
                .andExpect(jsonPath("$.comments").isArray)
                .andExpect(jsonPath("$.comments[0]").doesNotExist())

    }

    @Test
    open fun adminDeleteCommentFailNotAdmin() {

        val posting = postingService.savePostingWithLocationAndMedia("Test", null, user.account, null, LocalDateTime.now())
//        val comment = commentService.createComment("TestComment", LocalDateTime.now(), posting, user.account)
        val comment = postingService.addComment(posting, user.account, LocalDateTime.now(), "Hello!")
        val requestPosting = get("/posting/${posting.id}/")

        mockMvc.perform(requestPosting)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.text").exists())
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.user").exists())
                .andExpect(jsonPath("$.comments").isArray)
                .andExpect(jsonPath("$.comments[0].text").value("Hello!"))

        fun MockHttpServletResponse.asMap(): Map<String, Any> {
            val mapper = ObjectMapper()
            val body = this.contentAsString

            val json: Map<String, Any> = mapper.readValue(body, Map::class.java) as Map<String, Any>
            return json
        }

        val comments: List<Map<String, Any>> = mockMvc.perform(get("/posting/${posting.id}/"))
                .andReturn().response.asMap()["comments"]!! as List<Map<String, Any>>

        val commentId = comments.first()["id"]!!


        val requestDelete = MockMvcRequestBuilders
                .delete("/posting/${posting.id}/comment/$commentId/")
                .asUser(mockMvc, user.email, "password")

        mockMvc.perform(requestDelete)
                .andExpect(status().isForbidden)

    }

    @Test
    open fun createNewPostingWithTextAndLocationAndMedia() {
        val postData = mapOf(
                "text" to "TestPost",
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                "postingLocation" to mapOf(
                        "latitude" to 1.0,
                        "longitude" to 1.0
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
                .andExpect(jsonPath("$.postingLocation.locationData").exists())
                .andExpect(jsonPath("$.postingLocation.locationData.COUNTRY").value("Germany"))
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
    open fun createNewPostingWithEmptyTextFail() {
        val data = mapOf(
                "text" to "",
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        )

        val request = post("/posting/")
                .asUser(mockMvc, user.email, "password")
                .json(data)

        val response = mockMvc.perform(request)
                .andExpect(status().isBadRequest)
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    open fun createNewPostingWithTextAndHashtags() {
        val postData = mapOf(
                "text" to "hello #breakout bla #bräkauß blub #awsome",
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        )

        val request = post("/posting/")
                .asUser(mockMvc, user.email, "password")
                .json(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.text").value("hello #breakout bla #bräkauß blub #awsome"))
                .andExpect(jsonPath("$.hashtags").isArray)
                .andExpect(jsonPath("$.hashtags[0]").value("breakout"))
                .andExpect(jsonPath("$.hashtags[1]").value("bräkauß"))
                .andExpect(jsonPath("$.hashtags[2]").value("awsome"))
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.user").exists())
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    open fun getPostingsByHashTag() {
        postingService.savePostingWithLocationAndMedia("hello #breakout", null, user.account, null, LocalDateTime.now())
        postingService.savePostingWithLocationAndMedia("hello #awsome", null, user.account, null, LocalDateTime.now())

        val request = MockMvcRequestBuilders
                .get("/posting/hashtag/breakout/")

        val response = mockMvc.perform(request)
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
    open fun createNewPostingWithMediaApi() {
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
                        "latitude" to 1.0,
                        "longitude" to 1.0
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
                .andExpect(jsonPath("$.postingLocation.locationData").exists())
                .andExpect(jsonPath("$.postingLocation.locationData.COUNTRY").value("Germany"))
                .andExpect(jsonPath("$.postingLocation.date").exists())
                .andExpect(jsonPath("$.postingLocation.distance").exists())
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.user").exists())
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    open fun createNewPostingWith00LocationFail() {
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
                .andExpect(status().isBadRequest)
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
        val posting = postingService.savePostingWithLocationAndMedia("Test", Coord(1.0, 1.0), user.account, null, LocalDateTime.now())

        //when
        val request = get("/posting/${posting.id}/")

        //then
        val response = mockMvc.perform(request)
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
                .andExpect(jsonPath("$.postingLocation.locationData").exists())
                .andExpect(jsonPath("$.postingLocation.locationData.COUNTRY").value("Germany"))
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    open fun createNewPostingWithMediaAndAddMediaSizesWithoutToken() {
        //given

        val posting = postingService.savePostingWithLocationAndMedia("Test", Coord(1.0, 1.0), user.account, null, LocalDateTime.now());
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
        val request = post("/media/${savedposting!!.media.first().id}/")
                .json(postData)

        //then
        val response = mockMvc.perform(request)
                .andExpect(status().isBadRequest)
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    open fun createNewPostingWithMediaAndAddMediaSizesWithWrongToken() {


        val posting = postingService.savePostingWithLocationAndMedia("Test", Coord(1.0, 1.0), user.account, null, LocalDateTime.now());
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

        val request = post("/media/${savedposting!!.media.first().id}/")
                .header("X-UPLOAD-TOKEN", "87654321")
                .json(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isUnauthorized)
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    open fun createNewPostingWithMedia() {

        val posting = postingService.createPosting(user.account, "Test", arrayListOf("VIDEO"), null, LocalDateTime.now())

        val requestMedia = get("/posting/${posting.id}/")

        val responseMedia = mockMvc.perform(requestMedia)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.media").exists())
                .andExpect(jsonPath("$.media").isArray)
                .andExpect(jsonPath("$.media[0]").exists())
                .andExpect(jsonPath("$.media[0].id").exists())
                .andExpect(jsonPath("$.media[0].type").exists())
                .andExpect(jsonPath("$.media[0].sizes").exists())
                .andExpect(jsonPath("$.media[0].sizes").isArray)
                .andReturn().response.contentAsString

        println(responseMedia)
    }


    @Test
    open fun createNewPostingWithMediaAndAddMediaSizesWithValidToken() {


        val posting = postingService.savePostingWithLocationAndMedia("Test", Coord(1.0, 1.0), user.account, null, LocalDateTime.now())
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

        val request = post("/media/${savedposting!!.media.first().id}/")
                .header("X-UPLOAD-TOKEN", JWTSigner(JWT_SECRET).sign(mapOf("subject" to posting.media.first().id.toString()), JWTSigner.Options().setAlgorithm(Algorithm.HS512)))
                .json(postData)

        val response = mockMvc.perform(request)
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

        val responseMedia = mockMvc.perform(requestMedia)
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
    open fun createNewPostingWithMediaAndAddManyMediaSizes() {


        val posting = postingService.savePostingWithLocationAndMedia("Test", Coord(1.0, 1.0), user.account, null, LocalDateTime.now());
        val media = mediaService.createMedia("image")
        posting.media = listOf(media) as MutableList<Media>
        val savedposting = postingService.save(posting)

        for (i in 0..100) {

            val postData = mapOf(
                    "url" to "https://aws.amazon.com/bla$i.jpg",
                    "width" to i * 100 + 100,
                    "height" to 200,
                    "length" to 0.0,
                    "size" to 0.0,
                    "type" to "image"
            )

            val request = post("/media/${savedposting!!.media.first().id}/")
                    .header("X-UPLOAD-TOKEN", JWTSigner(JWT_SECRET).sign(mapOf("subject" to posting.media.first().id.toString()), JWTSigner.Options().setAlgorithm(Algorithm.HS512)))
                    .json(postData)

            mockMvc.perform(request)
        }


        val requestMedia = get("/posting/${savedposting!!.id}/")

        val responseMedia = mockMvc.perform(requestMedia)
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
                .andExpect(jsonPath("$.media[0].sizes[99]").exists())
                .andExpect(jsonPath("$.media[0].sizes[100]").doesNotExist())
                .andReturn().response.contentAsString

        println(responseMedia)
    }

    @Test
    open fun createNewPostingWithMediaAndAddMediaSizesUpdateMediaSizes() {


        val posting = postingService.savePostingWithLocationAndMedia("Test", Coord(1.0, 1.0), user.account, null, LocalDateTime.now());
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

        var request = post("/media/${savedposting!!.media.first().id}/")
                .header("X-UPLOAD-TOKEN", JWTSigner(JWT_SECRET).sign(mapOf("subject" to posting.media.first().id.toString()), JWTSigner.Options().setAlgorithm(Algorithm.HS512)))
                .json(postData)

        var response = mockMvc.perform(request)
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

        request = post("/media/${savedposting.media.first().id}/")
                .header("X-UPLOAD-TOKEN", JWTSigner(JWT_SECRET).sign(mapOf("subject" to posting.media.first().id.toString()), JWTSigner.Options().setAlgorithm(Algorithm.HS512)))
                .json(postData)

        response = mockMvc.perform(request)
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

        val responseMedia = mockMvc.perform(requestMedia)
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


        val posting = postingService.savePostingWithLocationAndMedia("Test", Coord(1.0, 1.0), user.account, null, LocalDateTime.now());
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

        var request = post("/media/${savedposting!!.media.first().id}/")
                .header("X-UPLOAD-TOKEN", JWTSigner(JWT_SECRET).sign(mapOf("subject" to posting.media.first().id.toString()), JWTSigner.Options().setAlgorithm(Algorithm.HS512)))
                .json(postData)

        var response = mockMvc.perform(request)
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

        request = post("/media/${savedposting.media.first().id}/")
                .header("X-UPLOAD-TOKEN", JWTSigner(JWT_SECRET).sign(mapOf("subject" to posting.media.first().id.toString()), JWTSigner.Options().setAlgorithm(Algorithm.HS512)))
                .json(postData)

        response = mockMvc.perform(request)
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

        val responseMedia = mockMvc.perform(requestMedia)
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


        val posting = postingService.savePostingWithLocationAndMedia("Test", Coord(1.0, 1.0), user.account, null, LocalDateTime.now());
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

        var request = post("/media/${savedposting!!.media.first().id}/")
                .header("X-UPLOAD-TOKEN", JWTSigner(JWT_SECRET).sign(mapOf("subject" to posting.media.first().id.toString()), JWTSigner.Options().setAlgorithm(Algorithm.HS512)))
                .json(postData)

        var response = mockMvc.perform(request)
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

        request = post("/media/${savedposting.media.first().id}/")
                .header("X-UPLOAD-TOKEN", JWTSigner(JWT_SECRET).sign(mapOf("subject" to posting.media.first().id.toString()), JWTSigner.Options().setAlgorithm(Algorithm.HS512)))
                .json(postData)

        response = mockMvc.perform(request)
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

        val responseMedia = mockMvc.perform(requestMedia)
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
    open fun onlyShowPostingsForQueryParamEvent() {
        val eventMuc = eventService.createEvent("Muc", LocalDateTime.now(), "Muc", Coord(1.1, 2.2), 36)
        val eventBer = eventService.createEvent("Ber", LocalDateTime.now(), "Berlin", Coord(1.1, 2.2), 36)

        val participant1 = userService.create("part1@example.com", "pw", { addRole(Participant::class) }).getRole(Participant::class)!!
        val participant2 = userService.create("part2@example.com", "pw", { addRole(Participant::class) }).getRole(Participant::class)!!
        val participant3 = userService.create("part3@example.com", "pw", { addRole(Participant::class) }).getRole(Participant::class)!!
        val participant4 = userService.create("part4@example.com", "pw", { addRole(Participant::class) }).getRole(Participant::class)!!

        val teamMuc = teamService.create(participant1, "", "", eventMuc)
        setAuthenticatedUser(participant1.email)
        teamService.invite(EmailAddress(participant2.email), teamMuc)

        setAuthenticatedUser(participant2.email)
        teamService.join(participant2, teamMuc)

        val teamBer = teamService.create(participant3, "", "", eventBer)
        setAuthenticatedUser(participant3.email)
        teamService.invite(EmailAddress(participant4.email), teamBer)

        setAuthenticatedUser(participant4.email)
        teamService.join(participant4, teamBer)

        postingService.createPosting(participant1, "posting for munich", null, null, LocalDateTime.now())
        postingService.createPosting(participant3, "posting for berlin", null, null, LocalDateTime.now())

        val request = get("/posting/")
                .param("event", eventMuc.id.toString())

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.[0].text").value("posting for munich"))
                .andExpect(jsonPath("$.[0]").exists())
                .andExpect(jsonPath("$.[1]").doesNotExist())

        val request2 = get("/posting/")
                .param("event", eventBer.id.toString())

        mockMvc.perform(request2)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.[0].text").value("posting for berlin"))
                .andExpect(jsonPath("$.[0]").exists())
                .andExpect(jsonPath("$.[1]").doesNotExist())

        val request3 = get("/posting/")
                .param("event", eventMuc.id.toString())
                .param("event", eventBer.id.toString())

        mockMvc.perform(request3)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.[0]").exists())
                .andExpect(jsonPath("$.[1]").exists())

    }

    @Test
    open fun getAllPostingsDefaultPageSize() {

        for (i in 1..200) {
            postingService.savePostingWithLocationAndMedia("Text $i", null, user.account, null, LocalDateTime.now())
        }

        val request = get("/posting/")

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[49]").exists())
                .andExpect(jsonPath("$[50]").doesNotExist())
                .andReturn().response.contentAsString
    }

    @Test
    open fun getAllPostingsLastPage() {

        for (i in 1..200) {
            postingService.savePostingWithLocationAndMedia("Text $i", null, user.account, null, LocalDateTime.now())
        }

        val request = get("/posting/?page=3")

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[49]").exists())
                .andExpect(jsonPath("$[50]").doesNotExist())
                .andReturn().response.contentAsString
    }

    @Test
    open fun getAllPostingsNonexistentPage() {

        for (i in 1..200) {
            postingService.savePostingWithLocationAndMedia("Text $i", null, user.account, null, LocalDateTime.now())
        }

        val request = get("/posting/?page=4")

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$[0]").doesNotExist())
                .andReturn().response.contentAsString
    }


    @Test
    open fun createNewComment() {

        val posting = postingService.savePostingWithLocationAndMedia("Test", null, user.account, null, LocalDateTime.now())

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
//                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.text").value("TestComment"))
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.user").exists())
                .andReturn().response.contentAsString

        println(response)


        val requestPosting = get("/posting/${posting.id}/")

        val responsePosting = mockMvc.perform(requestPosting)
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
    open fun createEmptyCommentFail() {

        val posting = postingService.savePostingWithLocationAndMedia("Test", null, user.account, null, LocalDateTime.now())

        val postData = mapOf(
                "text" to "",
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        )

        val request = post("/posting/${posting.id}/comment/")
                .asUser(mockMvc, user.email, "password")
                .json(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isBadRequest)
                .andReturn().response.contentAsString

        println(response)
    }


    @Test
    open fun createNewLike() {

        val posting = postingService.savePostingWithLocationAndMedia("Test", null, user.account, null, LocalDateTime.now())

        val postData = mapOf(
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        )

        val request = post("/posting/${posting.id}/like/")
                .asUser(mockMvc, user.email, "password")
                .json(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
//                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.user").exists())
                .andReturn().response.contentAsString

        println(response)


        val requestPosting = get("/posting/${posting.id}/")

        val responsePosting = mockMvc.perform(requestPosting)
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

        val posting = postingService.savePostingWithLocationAndMedia("Test", null, user.account, null, LocalDateTime.now())

        val postData = mapOf(
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        )

        val request = post("/posting/${posting.id}/like/")
                .asUser(mockMvc, user.email, "password")
                .json(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.user").exists())
                .andReturn().response.contentAsString

        println(response)


        val requestPosting = get("/posting/${posting.id}/?userid=${user.account.id}")

        val responsePosting = mockMvc.perform(requestPosting)
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
    open fun deleteLike() {

        val posting = postingService.savePostingWithLocationAndMedia("Test", null, user.account, null, LocalDateTime.now())

        val postData = mapOf(
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        )

        val request = post("/posting/${posting.id}/like/")
                .asUser(mockMvc, user.email, "password")
                .json(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.user").exists())
                .andReturn().response.contentAsString

        println(response)


        val requestPosting = get("/posting/${posting.id}/?userid=${user.account.id}")

        val responsePosting = mockMvc.perform(requestPosting)
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

        val requestDelete = delete("/posting/${posting.id}/like/")
                .asUser(mockMvc, user.email, "password")

        val responseDelete = mockMvc.perform(requestDelete)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.message").value("success"))
                .andReturn().response.contentAsString

        println(responseDelete)

        mockMvc.perform(requestPosting)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.text").exists())
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.user").exists())
                .andExpect(jsonPath("$.likes").exists())
                .andExpect(jsonPath("$.likes").value(0))
                .andExpect(jsonPath("$.hasLiked").value(false))
                .andReturn().response.contentAsString
    }

    @Test
    open fun createNewLikeFailDuplicate() {

        val posting = postingService.savePostingWithLocationAndMedia("Test", null, user.account, null, LocalDateTime.now())

        val postData = mapOf(
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        )

        val request = post("/posting/${posting.id}/like/")
                .asUser(mockMvc, user.email, "password")
                .json(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.user").exists())
                .andReturn().response.contentAsString

        println(response)


        val requestSecond = post("/posting/${posting.id}/like/")
                .asUser(mockMvc, user.email, "password")
                .json(postData)

        val responseSecond = mockMvc.perform(requestSecond)
                .andExpect(status().isConflict)
                .andReturn().response.contentAsString

        println(responseSecond)
    }

    @Test
    open fun getLikesForPosting() {
        val posting = postingService.savePostingWithLocationAndMedia("Test", null, user.account, null, LocalDateTime.now())
        postingService.like(posting, user.account, LocalDateTime.now())

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

    @Test
    fun postingDoesNotAllowHtmlAsPayload() {

        @Language("HTML")
        val body = mapOf(
                "text" to """<script type="text/javascript">alert("Such XSS. Much Wow");</script>""",
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        )

        val request = post("/posting/")
                .json(body)
                .asUser(this.mockMvc, user.email, "password")

        mockMvc.perform(request)
                .andExpect(status().isBadRequest)

    }
}
