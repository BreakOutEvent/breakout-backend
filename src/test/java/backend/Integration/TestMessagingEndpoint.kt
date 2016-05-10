@file:JvmName("TestMessagingEndpoint")

package backend.Integration

import org.junit.Before
import org.junit.Test
import org.springframework.http.HttpMethod
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class TestMessagingEndpoint : IntegrationTest() {

    @Before
    override fun setUp() {
        super.setUp()
    }

    @Test
    fun createNewGroupMessage() {
        val userCredentials = createUser(this.mockMvc, userService = userService)
        val user = userRepository.findOne(userCredentials.id.toLong())


        val postData = ArrayList<Long>().toJsonString()

        val request = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/messaging/")
                .header("Authorization", "Bearer ${userCredentials.accessToken}")
                .contentType(APPLICATION_JSON_UTF_8)
                .content(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.messages").isArray)
                .andExpect(jsonPath("$.users").isArray)
                .andExpect(jsonPath("$.users[0].id").value(user.core.id!!.toInt()))
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    fun createNewGroupMessageWithAdditionalUser() {
        val userCredentials = createUser(this.mockMvc, userService = userService)
        val user = userRepository.findOne(userCredentials.id.toLong())

        val userCredentials1 = createUser(this.mockMvc, email = "user1@mail.com", userService = userService)
        val user1 = userRepository.findOne(userCredentials1.id.toLong())

        val postData = listOf(user1.id).toJsonString()
        val request = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/messaging/")
                .header("Authorization", "Bearer ${userCredentials.accessToken}")
                .contentType(APPLICATION_JSON_UTF_8)
                .content(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.messages").isArray)
                .andExpect(jsonPath("$.users").isArray)
                .andExpect(jsonPath("$.users[0].id").value(user.core.id!!.toInt()))
                .andExpect(jsonPath("$.users[1].id").value(user1.core.id!!.toInt()))
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    fun getGroupMessage() {
        val userCredentials = createUser(this.mockMvc, userService = userService)
        val user = userRepository.findOne(userCredentials.id.toLong())
        val groupMessage = groupMessageService.createGroupMessage(user.core)

        val postData = ArrayList<Long>().toJsonString()

        val request = MockMvcRequestBuilders
                .request(HttpMethod.GET, "/messaging/${groupMessage.id}/")
                .header("Authorization", "Bearer ${userCredentials.accessToken}")
                .contentType(APPLICATION_JSON_UTF_8)
                .content(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.messages").isArray)
                .andExpect(jsonPath("$.users").isArray)
                .andExpect(jsonPath("$.users[0].id").value(user.core.id!!.toInt()))
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    fun getGroupMessageFailWrongUser() {
        val userCredentials = createUser(this.mockMvc, userService = userService)
        val user = userRepository.findOne(userCredentials.id.toLong())
        val userCredentials1 = createUser(this.mockMvc, email = "user1@mail.com", userService = userService)
        val groupMessage = groupMessageService.createGroupMessage(user.core)

        val postData = ArrayList<Long>().toJsonString()

        val request = MockMvcRequestBuilders
                .request(HttpMethod.GET, "/messaging/${groupMessage.id}/")
                .header("Authorization", "Bearer ${userCredentials1.accessToken}")
                .contentType(APPLICATION_JSON_UTF_8)
                .content(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isUnauthorized)
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    fun getGroupMessageFailMessageNotFound() {
        val userCredentials = createUser(this.mockMvc, userService = userService)
        val user = userRepository.findOne(userCredentials.id.toLong())
        groupMessageService.createGroupMessage(user.core)

        val postData = ArrayList<Long>().toJsonString()

        val request = MockMvcRequestBuilders
                .request(HttpMethod.GET, "/messaging/0/")
                .header("Authorization", "Bearer ${userCredentials.accessToken}")
                .contentType(APPLICATION_JSON_UTF_8)
                .content(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isNotFound)
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    fun addUserToGroupMessage() {
        val userCredentials = createUser(this.mockMvc, userService = userService)
        val user = userRepository.findOne(userCredentials.id.toLong())
        val groupMessage = groupMessageService.createGroupMessage(user.core)

        val userCredentials1 = createUser(this.mockMvc, email = "user1@mail.com", userService = userService)
        val user1 = userRepository.findOne(userCredentials1.id.toLong())

        val postData = listOf(user1.id).toJsonString()

        val request = MockMvcRequestBuilders
                .request(HttpMethod.PUT, "/messaging/${groupMessage.id}/")
                .header("Authorization", "Bearer ${userCredentials.accessToken}")
                .contentType(APPLICATION_JSON_UTF_8)
                .content(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.messages").isArray)
                .andExpect(jsonPath("$.users").isArray)
                .andExpect(jsonPath("$.users[0].id").value(user.core.id!!.toInt()))
                .andExpect(jsonPath("$.users[1].id").value(user1.core.id!!.toInt()))
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    fun addUserToGroupMessageFailWrongUser() {
        val userCredentials = createUser(this.mockMvc, userService = userService)
        val user = userRepository.findOne(userCredentials.id.toLong())
        val groupMessage = groupMessageService.createGroupMessage(user.core)

        val userCredentials1 = createUser(this.mockMvc, email = "user1@mail.com", userService = userService)
        val user1 = userRepository.findOne(userCredentials1.id.toLong())

        val postData = listOf(user1.id).toJsonString()

        val request = MockMvcRequestBuilders
                .request(HttpMethod.PUT, "/messaging/${groupMessage.id}/")
                .header("Authorization", "Bearer ${userCredentials1.accessToken}")
                .contentType(APPLICATION_JSON_UTF_8)
                .content(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isUnauthorized)
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    fun addMessageToGroupMessage() {
        val userCredentials = createUser(this.mockMvc, userService = userService)
        val user = userRepository.findOne(userCredentials.id.toLong())
        val groupMessage = groupMessageService.createGroupMessage(user.core)

        val postData = mapOf(
                "text" to "message Text",
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        ).toJsonString()

        val request = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/messaging/${groupMessage.id}/message/")
                .header("Authorization", "Bearer ${userCredentials.accessToken}")
                .contentType(APPLICATION_JSON_UTF_8)
                .content(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.messages[0].creator.id").value(user.core.id!!.toInt()))
                .andExpect(jsonPath("$.messages[0].date").exists())
                .andExpect(jsonPath("$.messages[0].text").value("message Text"))
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    fun addMessageToGroupMessageFailMessageNotFound() {
        val userCredentials = createUser(this.mockMvc, userService = userService)
        val user = userRepository.findOne(userCredentials.id.toLong())
        groupMessageService.createGroupMessage(user.core)

        val postData = mapOf(
                "text" to "message Text",
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        ).toJsonString()

        val request = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/messaging/0/message/")
                .header("Authorization", "Bearer ${userCredentials.accessToken}")
                .contentType(APPLICATION_JSON_UTF_8)
                .content(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isNotFound)
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    fun addMessageToGroupMessageFailWrongUser() {
        val userCredentials = createUser(this.mockMvc, userService = userService)
        val user = userRepository.findOne(userCredentials.id.toLong())
        val groupMessage = groupMessageService.createGroupMessage(user.core)

        val userCredentials1 = createUser(this.mockMvc, email = "user1@mail.com", userService = userService)

        val postData = mapOf(
                "text" to "message Text",
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        ).toJsonString()

        val request = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/messaging/${groupMessage.id}/message/")
                .header("Authorization", "Bearer ${userCredentials1.accessToken}")
                .contentType(APPLICATION_JSON_UTF_8)
                .content(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isUnauthorized)
                .andReturn().response.contentAsString

        println(response)
    }
}
