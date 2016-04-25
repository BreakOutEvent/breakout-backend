@file:JvmName("TestPostingEndpoint")

package backend.Integration

import backend.model.media.Media
import backend.model.misc.Coord
import backend.model.user.Admin
import backend.model.user.Participant
import backend.model.user.User
import backend.services.ConfigurationService
import com.auth0.jwt.Algorithm
import com.auth0.jwt.JWTSigner
import org.hamcrest.Matchers.hasSize
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

open class TestPostingEndpoint : IntegrationTest() {

    @Autowired
    private lateinit var configurationService: ConfigurationService
    private lateinit var JWT_SECRET: String
    private lateinit var userCredentials: Credentials
    private lateinit var user: User


    @Before
    override fun setUp() {
        super.setUp()
        val event = eventService.createEvent(
                title = "Breakout München",
                date = LocalDateTime.now(),
                city = "Munich",
                startingLocation = Coord(0.0, 0.0),
                duration = 36)

        this.JWT_SECRET = configurationService.getRequired("org.breakout.api.jwt_secret")
        userCredentials = createUser(this.mockMvc, userService = userService)

        makeUserParticipant(userCredentials)
        val creator = userRepository.findOne(userCredentials.id.toLong()).getRole(Participant::class)!!
        teamService.create(creator, "name", "description", event)

        getTokens(mockMvc, creator.email, "password").first
        getTokens(mockMvc, creator.email, "password").second

        userService.create("test_admin@break-out.org", "password", {
            addRole(Admin::class)
            isBlocked = false
        })
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
                "media" to arrayOf(
                        "image",
                        "image",
                        "audio"
                )
        ).toJsonString()

        val request = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/posting/")
                .header("Authorization", "Bearer ${userCredentials.accessToken}")
                .contentType(APPLICATION_JSON)
                .content(postData)

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
        val postData = mapOf(
                "text" to "TestPost",
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        ).toJsonString()

        val request = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/posting/")
                .header("Authorization", "Bearer ${userCredentials.accessToken}")
                .contentType(APPLICATION_JSON)
                .content(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.text").exists())
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.user").exists())
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    open fun createNewPostingWithMedia() {
        val postData = mapOf(
                "media" to arrayOf(
                        "image",
                        "image",
                        "audio"
                ),
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        ).toJsonString()

        val request = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/posting/")
                .header("Authorization", "Bearer ${userCredentials.accessToken}")
                .contentType(APPLICATION_JSON)
                .content(postData)

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
        ).toJsonString()

        val request = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/posting/")
                .header("Authorization", "Bearer ${userCredentials.accessToken}")
                .contentType(APPLICATION_JSON)
                .content(postData)

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
        ).toJsonString()

        val request = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/posting/")
                .header("Authorization", "Bearer ${userCredentials.accessToken}")
                .contentType(APPLICATION_JSON)
                .content(postData)

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
        ).toJsonString()

        val request = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/posting/")
                .header("Authorization", "Bearer invalidToken")
                .contentType(APPLICATION_JSON)
                .content(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isUnauthorized)
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    open fun getPostingById() {
        val user = userService.create("test@mail.com", "password", {
            addRole(Participant::class)
        })
        val event = eventService.createEvent("name", LocalDateTime.now(), "City", Coord(0.0, 0.0), 36)
        val team = teamService.create(user.getRole(Participant::class)!!, "name", "description", event)
        val posting = postingService.createPosting("Test", Coord(0.0, 0.0), user.core, null, 0.0, LocalDateTime.now())

        val request = MockMvcRequestBuilders
                .request(HttpMethod.GET, "/posting/" + posting.id + "/")
                .contentType(APPLICATION_JSON)

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

        val user = userService.create("test@mail.com", "password", {
            addRole(Participant::class)
        })
        val event = eventService.createEvent("name", LocalDateTime.now(), "City", Coord(0.0, 0.0), 36)
        val team = teamService.create(user.getRole(Participant::class)!!, "name", "description", event)

        val postingZero = postingService.createPosting("Test0", Coord(0.0, 0.0), user.core, null, 0.0, LocalDateTime.now())
        postingService.createPosting("Test1", Coord(0.0, 0.0), user.core, null, 0.0, LocalDateTime.now())
        val postingTwo = postingService.createPosting("Test2", Coord(0.0, 0.0), user.core, null, 0.0, LocalDateTime.now())

        val postingsIds: List<Long> = listOf(postingZero.id!!, postingTwo.id!!)

        val request = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/posting/get/ids")
                .content(postingsIds.toJsonString())
                .contentType(APPLICATION_JSON)

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
        val user = userService.create("test@mail.com", "password", {
            addRole(Participant::class)
        })
        val event = eventService.createEvent("name", LocalDateTime.now(), "City", Coord(0.0, 0.0), 36)
        val team = teamService.create(user.getRole(Participant::class)!!, "name", "description", event)

        val postingZero = postingService.createPosting("Test0", Coord(0.0, 0.0), user.core, null, 0.0, LocalDateTime.now())
        postingService.createPosting("Test1", Coord(0.0, 0.0), user.core, null, 0.0, LocalDateTime.now())
        postingService.createPosting("Test2", Coord(0.0, 0.0), user.core, null, 0.0, LocalDateTime.now())

        val request = MockMvcRequestBuilders
                .request(HttpMethod.GET, "/posting/get/since/${postingZero.id}/")
                .contentType(APPLICATION_JSON)

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

        val user = userService.create("test@mail.com", "password", {
            addRole(Participant::class)
        })
        val event = eventService.createEvent("name", LocalDateTime.now(), "City", Coord(0.0, 0.0), 36)
        val team = teamService.create(user.getRole(Participant::class)!!, "name", "description", event)

        val posting = postingService.createPosting("Test", Coord(0.0, 0.0), user.core, null, 0.0, LocalDateTime.now());
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
        ).toJsonString()

        val request = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/media/${savedposting!!.media!!.first().id}/")
                .contentType(APPLICATION_JSON)
                .content(postData)

        val response = mockMvc.perform (request)
                .andExpect(status().isBadRequest)
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    open fun createNewPostingWithMediaAndAddMediaSizesWithWrongToken() {

        val user = userService.create("test@mail.com", "password", {
            addRole(Participant::class)
        })
        val event = eventService.createEvent("name", LocalDateTime.now(), "City", Coord(0.0, 0.0), 36)
        val team = teamService.create(user.getRole(Participant::class)!!, "name", "description", event)

        val posting = postingService.createPosting("Test", Coord(0.0, 0.0), user.core, null, 0.0, LocalDateTime.now());
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
        ).toJsonString()

        val request = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/media/${savedposting!!.media!!.first().id}/")
                .contentType(APPLICATION_JSON)
                .header("X-UPLOAD-TOKEN", "87654321")
                .content(postData)

        val response = mockMvc.perform (request)
                .andExpect(status().isUnauthorized)
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    open fun createNewPostingWithMediaAndAddMediaSizesWithValidToken() {

        val user = userService.create("test@mail.com", "password", {
            addRole(Participant::class)
        })
        val event = eventService.createEvent("title", LocalDateTime.now(), "location", Coord(0.0, 0.0), 36)
        teamService.create(user.getRole(Participant::class)!!, "name", "description", event)

        val posting = postingService.createPosting("Test", Coord(0.0, 0.0), user.core, null, 0.0, LocalDateTime.now());
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
        ).toJsonString()

        println(posting.media)

        val request = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/media/${savedposting!!.media!!.first().id}/")
                .contentType(APPLICATION_JSON)
                .header("X-UPLOAD-TOKEN", JWTSigner(JWT_SECRET).sign(mapOf("subject" to posting.media!!.first().id.toString()), JWTSigner.Options().setAlgorithm(Algorithm.HS512)))
                .content(postData)

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

        val requestMedia = MockMvcRequestBuilders
                .request(HttpMethod.GET, "/posting/${savedposting.id}/")
                .contentType(APPLICATION_JSON)

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

        val user = userService.create("test@mail.com", "password", {
            addRole(Participant::class)
        })
        val event = eventService.createEvent("title", LocalDateTime.now(), "location", Coord(0.0, 0.0), 36)
        teamService.create(user.getRole(Participant::class)!!, "name", "description", event)

        val posting = postingService.createPosting("Test", Coord(0.0, 0.0), user.core, null, 0.0, LocalDateTime.now());
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
        ).toJsonString()

        println(posting.media)

        var request = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/media/${savedposting!!.media!!.first().id}/")
                .contentType(APPLICATION_JSON)
                .header("X-UPLOAD-TOKEN", JWTSigner(JWT_SECRET).sign(mapOf("subject" to posting.media!!.first().id.toString()), JWTSigner.Options().setAlgorithm(Algorithm.HS512)))
                .content(postData)

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
        ).toJsonString()

        println(posting.media)

        request = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/media/${savedposting.media!!.first().id}/")
                .contentType(APPLICATION_JSON)
                .header("X-UPLOAD-TOKEN", JWTSigner(JWT_SECRET).sign(mapOf("subject" to posting.media!!.first().id.toString()), JWTSigner.Options().setAlgorithm(Algorithm.HS512)))
                .content(postData)

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
                .request(HttpMethod.GET, "/posting/${savedposting.id}/")
                .contentType(APPLICATION_JSON)

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
        val user = userService.create("test@mail.com", "password", {
            addRole(Participant::class)
        })
        val event = eventService.createEvent("name", LocalDateTime.now(), "City", Coord(0.0, 0.0), 36)
        val team = teamService.create(user.getRole(Participant::class)!!, "name", "description", event)
        postingService.createPosting("Test", Coord(0.0, 0.0), user.core, null, 0.0, LocalDateTime.now())
        postingService.createPosting("Test 2", Coord(0.0, 0.0), user.core, null, 0.0, LocalDateTime.now())

        val request = MockMvcRequestBuilders
                .request(HttpMethod.GET, "/posting/")
                .contentType(APPLICATION_JSON)

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[1]").exists())
                .andReturn().response.contentAsString
    }

    private fun makeUserParticipant(credentials: Credentials) {

        val date = LocalDate.now().toString()

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
                        "birthdate" to date,
                        "emergencynumber" to "0987654321"
                )
        ).toJsonString()

        println(json);

        val request = MockMvcRequestBuilders.put("/user/${credentials.id}/")
                .header("Authorization", "Bearer ${credentials.accessToken}")
                .contentType(APPLICATION_JSON)
                .content(json)

        val response = mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(credentials.id))
                .andExpect(jsonPath("$.firstname").value("Florian"))
                .andExpect(jsonPath("$.lastname").value("Schmidt"))
                .andExpect(jsonPath("$.gender").value("Male"))
                .andExpect(jsonPath("$.blocked").value(false))
                .andExpect(jsonPath("$.participant").exists())
                .andExpect(jsonPath("$.participant.tshirtsize").value("XL"))
                .andExpect(jsonPath("$.participant.hometown").value("Dresden"))
                .andExpect(jsonPath("$.participant.birthdate").value(date))
                .andExpect(jsonPath("$.participant.phonenumber").value("01234567890"))
                .andExpect(jsonPath("$.participant.emergencynumber").value("0987654321"))
                .andReturn().response.contentAsString

        println(response)
    }

}
