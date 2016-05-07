package backend.controller

import backend.Integration.IntegrationTest
import backend.Integration.createUser
import backend.Integration.toJsonString
import org.junit.Ignore
import org.junit.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class UserControllerTest : IntegrationTest() {

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
                                "street" to "Würzburger Straße"
                        )
                )
        ).toJsonString()

        val request = MockMvcRequestBuilders
                .put("/user/${credentials.id}/")
                .header("Authorization", "Bearer ${credentials.accessToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)

        val result = mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(credentials.id))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("a@x.de"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstname").value("Florian"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastname").value("Schmidt"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.gender").value("Male"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.blocked").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sponsor").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.sponsor.company").value("ABC GmBH"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sponsor.url").value("http://www.test.de"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sponsor.hidden").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sponsor.address").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.sponsor.address.city").value("Dresden"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sponsor.address.housenumber").value("79c"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sponsor.address.country").value("Germany"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sponsor.address.street").value("Würzburger Straße"))
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
