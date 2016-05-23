@file:JvmName("TestMessagingEndpoint")

package backend.Integration

import backend.testHelper.asUser
import backend.testHelper.json
import org.junit.Before
import org.junit.Test
import org.springframework.http.HttpMethod
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime
import java.time.ZoneOffset

class TestMessagingEndpoint : IntegrationTest() {

    @Before
    override fun setUp() {
        super.setUp()
    }

    @Test
    fun createNewGroupMessage() {

        val user = userService.create("user@break-out.org", "password")

        val request = post("/messaging/")
                .asUser(this.mockMvc, "user@break-out.org", "password")
                .json(listOf<Long>())

        val response = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.messages").isArray)
                .andExpect(jsonPath("$.users").isArray)
                .andExpect(jsonPath("$.users[0].id").value(user.core.id!!.toInt()))
                .andReturn().response.contentAsString

        println(response)

        val requestMe = get("/me/")
                .asUser(this.mockMvc, "user@break-out.org", "password")
                .json(listOf<Long>())

        mockMvc.perform(requestMe)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.groupMessageIds[0]").exists())
                .andExpect(jsonPath("$.groupMessageIds[1]").doesNotExist())
                .andReturn().response.contentAsString
    }

    @Test
    fun createNewGroupMessageWithAdditionalUser() {
        val user = userService.create("user@break-out.org", "password")
        val user1 = userService.create("user1@mail.com", "password")

        val data = listOf(user1.core.id)

        val request = post("/messaging/")
                .asUser(mockMvc, "user@break-out.org", "password")
                .json(data)

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
        val user = userService.create("user@break-out.org", "password")
        val groupMessage = groupMessageService.createGroupMessage(user.core)

        val request = get("/messaging/${groupMessage.id}/")
                .asUser(mockMvc, user.email, "password")
                .json(listOf())

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

        val user = userService.create("user@break-out.org", "password")
        val user1 = userService.create("user1@mail.com", "password")

        val groupMessage = groupMessageService.createGroupMessage(user.core)

        val request = get("/messaging/${groupMessage.id}/")
                .asUser(mockMvc, user1.email, "password")
                .json(listOf())

        val response = mockMvc.perform(request)
                .andExpect(status().isUnauthorized)
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    fun getGroupMessageFailMessageNotFound() {

        val user = userService.create("user@break-out.org", "password")
        groupMessageService.createGroupMessage(user.core)


        val request = request(HttpMethod.GET, "/messaging/0/")
                .asUser(mockMvc, user.email, "password")
                .json(listOf())

        val response = mockMvc.perform(request)
                .andExpect(status().isNotFound)
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    fun addUserToGroupMessage() {

        val user = userService.create("user@break-out.org", "password")
        val user1 = userService.create("user1@mail.com", "password")
        val groupMessage = groupMessageService.createGroupMessage(user.core)

        val postData = listOf(user1.core.id).toJsonString()

        val request = request(HttpMethod.PUT, "/messaging/${groupMessage.id}/")
                .asUser(mockMvc, user.email, "password")
                .json(postData)

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


        val requestGet = get("/messaging/${groupMessage.id}/")
                .asUser(mockMvc, user.email, "password")
                .json(listOf())

        mockMvc.perform(requestGet)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.messages").isArray)
                .andExpect(jsonPath("$.users").isArray)
                .andExpect(jsonPath("$.users[0].id").value(user.core.id!!.toInt()))
                .andExpect(jsonPath("$.users[1].id").value(user1.core.id!!.toInt()))
                .andReturn().response.contentAsString
    }

    @Test
    fun addUserToGroupMessageFailWrongUser() {

        val user = userService.create("user@break-out.org", "password")
        val user1 = userService.create("user1@mail.com", "password")

        val groupMessage = groupMessageService.createGroupMessage(user.core)

        val postData = listOf(user1.core.id).toJsonString()

        val request = put("/messaging/${groupMessage.id}/")
                .asUser(mockMvc, user1.email, "password")
                .json(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isUnauthorized)
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    fun addMessageToGroupMessage() {
        val user = userService.create("user@break-out.org", "password")
        val groupMessage = groupMessageService.createGroupMessage(user.core)

        val postData = mapOf("text" to "message Text", "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))

        val request = request(HttpMethod.POST, "/messaging/${groupMessage.id}/message/")
                .asUser(mockMvc, user.email, "password")
                .json(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.messages[0].creator.id").value(user.core.id!!.toInt()))
                .andExpect(jsonPath("$.messages[0].date").exists())
                .andExpect(jsonPath("$.messages[0].text").value("message Text"))
                .andReturn().response.contentAsString

        println(response)

        val requestGet = get("/messaging/${groupMessage.id}/")
                .asUser(mockMvc, user.email, "password")
                .json(listOf())

        mockMvc.perform(requestGet)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.messages[0].creator.id").value(user.core.id!!.toInt()))
                .andExpect(jsonPath("$.messages[0].date").exists())
                .andExpect(jsonPath("$.messages[0].text").value("message Text"))
                .andReturn().response.contentAsString
    }

    @Test
    fun addMessageToGroupMessageFailMessageNotFound() {
        val user = userService.create("user@break-out.org", "password")
        groupMessageService.createGroupMessage(user.core)

        val postData = mapOf("text" to "message Text", "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))

        val request = request(HttpMethod.POST, "/messaging/0/message/")
                .asUser(mockMvc, user.email, "password")
                .json(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isNotFound)
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    fun addMessageToGroupMessageFailWrongUser() {
        val user = userService.create("user@break-out.org", "password")
        val user1 = userService.create("user1@mail.com", "password")

        val groupMessage = groupMessageService.createGroupMessage(user.core)

        val postData = mapOf("text" to "message Text", "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))

        val request = request(HttpMethod.POST, "/messaging/${groupMessage.id}/message/")
                .asUser(mockMvc, user1.email, "password")
                .json(postData)

        val response = mockMvc.perform(request)
                .andExpect(status().isUnauthorized)
                .andReturn().response.contentAsString

        println(response)
    }
}
