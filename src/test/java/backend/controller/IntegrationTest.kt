@file:JvmName("IntegrationTest")
package backend.controller

import backend.TestBackendConfiguration
import backend.model.user.UserRepository
import org.junit.Before
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@RunWith(SpringJUnit4ClassRunner::class)
@SpringApplicationConfiguration(classes = arrayOf(TestBackendConfiguration::class))
@WebAppConfiguration
@org.springframework.boot.test.IntegrationTest("server.port:0")
abstract class IntegrationTest {


    @Autowired lateinit private var context: WebApplicationContext
    @Autowired lateinit protected var userRepository: UserRepository
    lateinit protected var mockMvc: MockMvc

    @Before
    open fun setUp() {
        userRepository.deleteAll()
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
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

