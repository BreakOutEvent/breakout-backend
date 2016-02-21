@file:JvmName("TestPostEndpoint")

package backend.Integration

import backend.model.misc.Coords
import backend.model.posting.Media
import backend.model.user.Admin
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import org.hamcrest.Matchers.hasSize
import org.junit.Ignore

class TestPostEndpoint : IntegrationTest() {

//    @Value("\${org.breakout.api.jwt_secret}")
    private var JWT_SECRET: String = System.getenv("RECODER_JWT_SECRET") ?: "testsecret"

    lateinit var userCredentials: Credentials

    val APPLICATION_JSON_UTF_8 = "application/json;charset=UTF-8"

    @Before
    override fun setUp() {
        super.setUp()
        userCredentials = createUser(this.mockMvc, userService = userService)

        userService.create("test_admin@break-out.org", "password", {
            addRole(Admin::class.java)
            isBlocked = false
        })
    }

    @Test
    fun createNewPostingWithTextAndLocationAndMedia() {
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
                .contentType(MediaType.APPLICATION_JSON)
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
    fun createNewPostingWithText() {
        val postData = mapOf(
                "text" to "TestPost",
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        ).toJsonString()

        val request = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/posting/")
                .header("Authorization", "Bearer ${userCredentials.accessToken}")
                .contentType(MediaType.APPLICATION_JSON)
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
    fun createNewPostingWithMedia() {
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
                .contentType(MediaType.APPLICATION_JSON)
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
    fun createNewPostingWithLocation() {
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
                .contentType(MediaType.APPLICATION_JSON)
                .content(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.postingLocation.latitude").exists())
                .andExpect(jsonPath("$.postingLocation.longitude").exists())
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.user").exists())
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    fun dontCreatePostingForInvalidJSON() {
        val postData = mapOf(
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        ).toJsonString()

        val request = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/posting/")
                .header("Authorization", "Bearer ${userCredentials.accessToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isBadRequest)
                .andReturn().response.contentAsString

        println(response)
    }


    @Test
    fun dontCreatePostingWithoutValidAuth() {
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
                .contentType(MediaType.APPLICATION_JSON)
                .content(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isUnauthorized)
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    fun getPostingById() {
        val user = userService.create("test@mail.com", "password")
        val posting = postingService.createPosting("Test", Coords(0.0, 0.0), user.core!!, null)

        val request = MockMvcRequestBuilders
                .request(HttpMethod.GET, "/posting/" + posting.id + "/")
                .contentType(MediaType.APPLICATION_JSON)

        val response = mockMvc.perform (request)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.text").exists())
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.user").exists())
                .andExpect(jsonPath("$.postingLocation.latitude").exists())
                .andExpect(jsonPath("$.postingLocation.longitude").exists())
                .andReturn().response.contentAsString

        println(response)
    }


    @Test
    fun getPostingsByIds() {
        val user = userService.create("test@mail.com", "password")
        val postingZero = postingService.createPosting("Test0", Coords(0.0, 0.0), user.core!!, null)
        val postingOne = postingService.createPosting("Test1", Coords(0.0, 0.0), user.core!!, null)
        val postingTwo = postingService.createPosting("Test2", Coords(0.0, 0.0), user.core!!, null)

        val postingsIds: List<Long> = listOf(postingZero.id!!, postingTwo.id!!)

        val request = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/posting/get/ids")
                .content(postingsIds.toJsonString())
                .contentType(MediaType.APPLICATION_JSON)

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
    fun getPostingIdsSince() {
        val user = userService.create("test@mail.com", "password")
        val postingZero = postingService.createPosting("Test0", Coords(0.0, 0.0), user.core!!, null)
        postingService.createPosting("Test1", Coords(0.0, 0.0), user.core!!, null)
        postingService.createPosting("Test2", Coords(0.0, 0.0), user.core!!, null)

        val request = MockMvcRequestBuilders
                .request(HttpMethod.GET, "/posting/get/since/${postingZero.id}/")
                .contentType(MediaType.APPLICATION_JSON)

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
    fun createNewPostingWithMediaAndAddMediaSizesWithoutToken() {

        val user = userService.create("test@mail.com", "password")
        val posting = postingService.createPosting("Test", Coords(0.0, 0.0), user.core!!, null);
        val media = mediaService.createMedia(posting, "image")
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
                .request(HttpMethod.POST, "/posting/media/${savedposting!!.media!!.first().id}/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(postData)

        val response = mockMvc.perform (request)
                .andExpect(status().isBadRequest)
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    fun createNewPostingWithMediaAndAddMediaSizesWithWrongToken() {

        val user = userService.create("test@mail.com", "password")
        val posting = postingService.createPosting("Test", Coords(0.0, 0.0), user.core!!, null);
        val media = mediaService.createMedia(posting, "image")
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
                .request(HttpMethod.POST, "/posting/media/${savedposting!!.media!!.first().id}/")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-UPLOAD-TOKEN", "87654321")
                .content(postData)

        val response = mockMvc.perform (request)
                .andExpect(status().isUnauthorized)
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    fun createNewPostingWithMediaAndAddMediaSizesWithValidToken() {

        val user = userService.create("test@mail.com", "password")
        val posting = postingService.createPosting("Test", Coords(0.0, 0.0), user.core!!, null);
        val media = mediaService.createMedia(posting, "image")
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
                .request(HttpMethod.POST, "/posting/media/${savedposting!!.media!!.first().id}/")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-UPLOAD-TOKEN", Jwts.builder().setSubject(savedposting.media!!.first().id.toString()).signWith(SignatureAlgorithm.HS512, JWT_SECRET).compact())
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
    }

    @Test
    fun getAllPostings() {
        val user = userService.create("test@mail.com", "password")
        val posting = postingService.createPosting("Test", Coords(0.0, 0.0), user.core!!, null)
        val secondPosting = postingService.createPosting("Test 2", Coords(0.0, 0.0), user.core!!, null)

        val request = MockMvcRequestBuilders
                .request(HttpMethod.GET, "/posting/")
                .contentType(MediaType.APPLICATION_JSON)

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[1]").exists())
                .andReturn().response.contentAsString
    }

}
