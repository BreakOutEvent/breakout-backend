@file:JvmName("TestUserController")
package backend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.hasSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.*

class TestUserController : IntegrationTest() {

    @Before
    override fun setUp() {
        super.setUp()
    }

    @Test
    @Throws(Exception::class)
    fun addUser() {

        val jsonMap = HashMap<String, String>()
        jsonMap.put("firstname", "Florian")
        jsonMap.put("lastname", "Schmidt")
        jsonMap.put("email", "florian.schmidt.1994@icloud.com")
        jsonMap.put("password", "mypassword")
        jsonMap.put("gender", "Male")

        val om = ObjectMapper()
        val json = om.writeValueAsString(jsonMap)

        this.mockMvc.perform(post("/test/user/", json)).andExpect(status().isCreated).andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE)).andExpect(jsonPath("id").exists())
    }

    @Test
    @Throws(Exception::class)
    fun dontCreateUserForInvalidBody() {

        // firstname is missing
        val jsonMap = HashMap<String, String>()
        jsonMap.put("lastname", "Schmidt")
        jsonMap.put("email", "florian.schmidt.1994@icloud.com")
        jsonMap.put("password", "mypassword")
        jsonMap.put("gender", "Male")

        val om = ObjectMapper()
        val json = om.writeValueAsString(jsonMap)

        this.mockMvc.perform(post("/test/user/", json)).andExpect(status().isBadRequest)
    }

    @Test
    @Throws(Exception::class)
    fun userGetsSavedToDatabase() {
        val jsonMap = HashMap<String, String>()
        jsonMap.put("firstname", "Florian")
        jsonMap.put("lastname", "Schmidt")
        jsonMap.put("email", "florianschmidt.1994@icloud.com")
        jsonMap.put("password", "mypassword")
        jsonMap.put("gender", "Male")

        val om = ObjectMapper()
        val json = om.writeValueAsString(jsonMap)

        val result = this.mockMvc.perform(post("/test/user/", json))
                .andExpect(status().isCreated)
                .andExpect(content()
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("id").exists())
                .andReturn()
        
        val user = userRepository.findByEmail("florianschmidt.1994@icloud.com")
        assertNotNull(user)
        assertEquals(user.email, jsonMap["email"])

    }

    //TODO: Testcases where email already exists in database!

    @Test
    @Throws(Exception::class)
    fun getAllUsers() {
        // Insert dummy users in database
        var jsonMap: MutableMap<String, String> = HashMap()
        jsonMap.put("firstname", "Florian")
        jsonMap.put("lastname", "Schmidt")
        jsonMap.put("email", "florian.schmidt.1994@icloud.com")
        jsonMap.put("password", "mypassword")
        jsonMap.put("gender", "Male")

        val om = ObjectMapper()
        var json = om.writeValueAsString(jsonMap)

        this.mockMvc.perform(post("/test/user/", json))
                .andExpect(status().isCreated)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("id").exists())
                .andReturn()

        jsonMap = HashMap<String, String>()
        jsonMap.put("firstname", "Florian")
        jsonMap.put("lastname", "Schmidt")
        jsonMap.put("email", "florianschmidt.1995@icloud.com")
        jsonMap.put("password", "mypassword")
        jsonMap.put("gender", "Male")

        json = om.writeValueAsString(jsonMap)
        this.mockMvc.perform(post("/test/user/", json))
                .andExpect(status().isCreated)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("id").exists())
                .andReturn()


        this.mockMvc.perform(get("/test/user/"))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$", hasSize<Any>(2)))
                .andExpect(jsonPath("$[0].email").exists())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].blocked").exists())
                .andExpect(jsonPath("$[0].firstname").exists())
                .andExpect(jsonPath("$[0].lastname").exists())
                .andExpect(jsonPath("$[0].password").exists())
                .andExpect(jsonPath("$[0].gender").exists())
                .andExpect(jsonPath("$[0].userRoles").exists())
                .andExpect(jsonPath("$[1].email").exists())
                .andExpect(jsonPath("$[1].id").exists())
                .andExpect(jsonPath("$[1].blocked").exists())
                .andExpect(jsonPath("$[1].firstname").exists())
                .andExpect(jsonPath("$[1].lastname").exists())
                .andExpect(jsonPath("$[1].password").exists())
                .andExpect(jsonPath("$[1].gender").exists())
                .andExpect(jsonPath("$[1].userRoles").exists())
                .andReturn()
    }
}
