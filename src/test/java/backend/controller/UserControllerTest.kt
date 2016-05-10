package backend.controller

import backend.Integration.IntegrationTest
import backend.Integration.createUser
import backend.Integration.toJsonString
import org.junit.Ignore
import org.junit.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class UserControllerTest : IntegrationTest() {

    // The ignored tests here are located in Integration.TestUserEndpoint
    // These should be moved to the corresponding controller test (e.g. here)
    // in the future. For now these auto generated tests will be left here ignored
    @Test
    @Ignore("Needs to be migrated from package integration")
    fun testCreateUser() {

    }

    @Test
    @Ignore("Needs to be migrated from package integration")
    fun testRequestPasswordReset() {

    }

    @Test
    @Ignore("Needs to be migrated from package integration")
    fun testResetPassword() {

    }

    @Test
    @Ignore("Needs to be migrated from package integration")
    fun testShowUsers() {

    }

    @Test
    fun testUpdateUserAndMakeHimSponsor() {

        val credentials = createUser(this.mockMvc, userService = userService)

        // Update user with role sponsor
        val json = mapOf(
                "firstname" to "Florian",
                "lastname" to "Schmidt",
                "gender" to "Male",
                "blocked" to false,
                "sponsor" to mapOf(
                        "company" to "ABC GmBH",
                        "url" to "http://www.test.de",
                        "isHidden" to false,
                        "address" to mapOf(
                                "city" to "Dresden",
                                "housenumber" to "79c",
                                "country" to "Germany",
                                "street" to "Würzburger Straße",
                                "zipcode" to "01189"
                        )
                )
        ).toJsonString()

        val request = MockMvcRequestBuilders
                .put("/user/${credentials.id}/")
                .header("Authorization", "Bearer ${credentials.accessToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)

        val result = mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(credentials.id))
                .andExpect(jsonPath("$.email").value("a@x.de"))
                .andExpect(jsonPath("$.firstname").value("Florian"))
                .andExpect(jsonPath("$.lastname").value("Schmidt"))
                .andExpect(jsonPath("$.gender").value("Male"))
                .andExpect(jsonPath("$.blocked").value(false))
                .andExpect(jsonPath("$.sponsor").exists())
                .andExpect(jsonPath("$.sponsor.company").value("ABC GmBH"))
                .andExpect(jsonPath("$.sponsor.url").value("http://www.test.de"))
                .andExpect(jsonPath("$.sponsor.hidden").value(false))
                .andExpect(jsonPath("$.sponsor.address").exists())
                .andExpect(jsonPath("$.sponsor.address.city").value("Dresden"))
                .andExpect(jsonPath("$.sponsor.address.housenumber").value("79c"))
                .andExpect(jsonPath("$.sponsor.address.country").value("Germany"))
                .andExpect(jsonPath("$.sponsor.address.street").value("Würzburger Straße"))
                .andExpect(jsonPath("$.sponsor.address.zipcode").value("01189"))
                .andReturn().response.contentAsString

        println(result)
    }


    @Test
    @Ignore("Needs to be migrated from package integration")
    fun testShowUser() {

    }

    @Test
    @Ignore("Needs to be migrated from package integration")
    fun testToAddress() {

    }

    @Test
    @Ignore("Needs to be migrated from package integration")
    fun testShowInvitation() {

    }
}
