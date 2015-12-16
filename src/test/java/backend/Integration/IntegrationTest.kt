package backend.Integration

import backend.TestBackendConfiguration
import backend.WebSecurityConfiguration
import backend.controller.RequestBodies.PostUserBody
import backend.model.user.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Before
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.IntegrationTest
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
@SpringApplicationConfiguration(classes = arrayOf(TestBackendConfiguration::class, WebSecurityConfiguration::class))
@WebAppConfiguration
@IntegrationTest("server.port:0")
abstract class IntegrationTest {


    @Autowired lateinit private var context: WebApplicationContext
    @Autowired lateinit protected var userRepository: UserRepository
    lateinit protected var mockMvc: MockMvc

    companion object {

        var counter = 0;

        fun getDummyPostUserBody(): PostUserBody {
            val body = PostUserBody().apply {
                email = "nr$counter@icloud.com"
                firstname = "Florian"
                lastname = "Schmidt"
                gender = "Male"
                password = "Awesome password"
            }
            counter++
            return body
        }
    }

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

// Add .toJsonString() to class map
fun Map<String, kotlin.Any>.toJsonString() = ObjectMapper().writeValueAsString(this)

