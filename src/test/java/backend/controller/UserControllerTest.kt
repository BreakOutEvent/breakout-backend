package backend.controller

import backend.Integration.IntegrationTest
import backend.model.misc.Coord
import backend.model.user.Participant
import backend.testHelper.asUser
import backend.testHelper.json
import org.joda.time.DateTime
import org.junit.Ignore
import org.junit.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime
import kotlin.reflect.KClass

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

        val user = userService.create("user@break-out.org", "password")

        // Update user with role sponsor
        val body = mapOf(
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
        )

        val request = put("/user/${user.account.id}/")
                .asUser(mockMvc, user.email, "password")
                .json(body)

        val result = mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(user.account.id!!.toInt()))
                .andExpect(jsonPath("$.email").value("user@break-out.org"))
                .andExpect(jsonPath("$.firstname").value("Florian"))
                .andExpect(jsonPath("$.lastname").value("Schmidt"))
                .andExpect(jsonPath("$.gender").value("Male"))
                .andExpect(jsonPath("$.blocked").value(true))
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
    fun updateUserSetPreferredLanguage() {
        val user = userService.create("test1@example.com", "pw")

        val body = mapOf(
                "preferredLanguage" to "en"
        )

        val request = put("/user/${user.account.id}/")
                .asUser(mockMvc, user.email, "pw")
                .json(body)

        mockMvc.perform(request)
                .andExpect(jsonPath("$.preferredLanguage").value("en"))
    }

    @Test
    fun createUserHasDefaultPreferredLanguageDe() {
        val user = userService.create("test2@example.com", "pw")
        val request = get("/me/")
                .asUser(this.mockMvc, user.email, "pw")

        mockMvc.perform(request)
                .andExpect(jsonPath("$.preferredLanguage").value("de"))
    }

    @Test
    @Ignore("Needs to be migrated from package integration")
    fun testShowUser() {

    }

    @Test
    fun testBlockUser() {

        val blocker = userService.create("test3@example.com", "pw", { addRole(Participant::class) })
        val blocked = userService.create("test4@example.com", "pw", { addRole(Participant::class) })

        val event = eventService.createEvent("Test Event",
                LocalDateTime.now(),
                "Munich",
                Coord(0.0, 0.0),
                36)

        val team = teamService.create(blocked.getRole(Participant::class) as Participant,
                "This is a test Team",
                "This is a test Team", event)

        postingService.createPosting(blocked, "Test From Blocked User", null, null, LocalDateTime.now())

        val request = post("/user/${blocked.account.id}/block/", "")
                .asUser(this.mockMvc, blocker.email, "pw")

        mockMvc.perform(request).andDo {
            val blocker = blocker.account.id?.let { userService.getUserById(it) }
            val blocked = blocked.account.id?.let { userService.getUserById(it) }

            // Test Blocked Status

            assert(blocked?.isBlockedBy(blocker?.account?.id) ?: false)

            // Test Team Filter

            val anonymousPostingRequest = get("/posting/")
            mockMvc.perform(anonymousPostingRequest).andExpect(jsonPath("$[0]").exists())

            val blockerPostingRequest = get("/posting/")
                    .asUser(this.mockMvc, blocker!!.email, "pw")
            mockMvc.perform(blockerPostingRequest).andExpect(jsonPath("$[0]").doesNotExist())

            // Test User Filter

            val anonymousBlockedRequest = get("/user/${blocked?.account?.id}/")
            mockMvc.perform(anonymousBlockedRequest).andExpect(jsonPath("$.id").exists())

            val blockerBlockedRequest = get("/user/${blocked?.account?.id}/")
                    .asUser(this.mockMvc, blocker!!.email, "pw")
            mockMvc.perform(blockerBlockedRequest).andExpect(jsonPath("$.id").doesNotExist())

            // Test Team Filter

            val anonymousTeamRequest = get("/event/${event.id}/team/")
            mockMvc.perform(anonymousTeamRequest).andExpect(jsonPath("$[0]").exists())

            val blockerTeamRequest = get("/event/${event.id}/team/")
                    .asUser(this.mockMvc, blocker!!.email, "pw")
            mockMvc.perform(blockerTeamRequest).andExpect(jsonPath("$[0]").doesNotExist())

        }

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
