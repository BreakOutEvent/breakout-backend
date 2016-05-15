package backend.controller

import backend.Integration.IntegrationTest
import backend.Integration.getTokens
import backend.Integration.toJsonString
import backend.model.misc.Coord
import backend.model.user.Participant
import backend.model.user.Sponsor
import backend.util.euroOf
import org.junit.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

class SponsoringControllerTest : IntegrationTest() {

    @Test
    fun testGetAllSponsorings() {

        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 0.0), 36)
        val participant = userService.create("participant@mail.de", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val sponsor = userService.create("sponsor@mail.de", "password", { addRole(Sponsor::class) }).getRole(Sponsor::class)!!
        val team = teamService.create(participant, "name", "description", event)
        sponsoringService.createSponsoring(sponsor, team, euroOf(1), euroOf(200))

        val tokens = getTokens(this.mockMvc, participant.email, "password")

        val request = get("/event/${event.id}/team/${team.id}/sponsoring/")
                .header("Authorization", "Bearer ${tokens.first}")

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.[0]").exists())
                .andExpect(jsonPath("$.[0].amountPerKm").exists())
                .andExpect(jsonPath("$.[0].limit").exists())
                .andExpect(jsonPath("$.[0].teamId").exists())
                .andExpect(jsonPath("$.[0].team").exists())
                .andExpect(jsonPath("$.[0].sponsorId").exists())

        val unauthorized = get("/event/${event.id}/team/${team.id}/sponsoring/")
        mockMvc.perform(unauthorized).andExpect(status().isUnauthorized)
    }

    @Test
    fun testCreateSponsoringWithRegisteredSponsor() {
        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 0.0), 36)
        val participant = userService.create("participant@mail.de", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val sponsor = userService.create("sponsor@mail.de", "password", { addRole(Sponsor::class) }).getRole(Sponsor::class)!!
        val team = teamService.create(participant, "name", "description", event)

        val tokens = getTokens(this.mockMvc, sponsor.email, "password")

        val body = mapOf(
                "amountPerKm" to 1.0,
                "limit" to 200
        ).toJsonString()

        val request = MockMvcRequestBuilders.post("/event/${event.id}/team/${team.id}/sponsoring/")
                .header("Authorization", "Bearer ${tokens.first}")
                .contentType(APPLICATION_JSON_UTF_8)
                .content(body)

        mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.amountPerKm").exists())
                .andExpect(jsonPath("$.limit").exists())
                .andExpect(jsonPath("$.teamId").exists())
                .andExpect(jsonPath("$.team").exists())
                .andExpect(jsonPath("$.sponsorId").exists())
                .andExpect(jsonPath("$.status").value("proposed"))
                .andExpect(jsonPath("$.unregisteredSponsor").doesNotExist())
    }

    @Test
    fun testCreateSponsoringWithUnregisteredSponsor() {
        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 0.0), 36)
        val participant = userService.create("participant@mail.de", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val team = teamService.create(participant, "name", "description", event)

        val body = mapOf(
                "amountPerKm" to 1.0,
                "limit" to 200,
                "unregisteredSponsor" to mapOf(
                        "firstname" to "Florian",
                        "lastname" to "Schmidt",
                        "url" to "www.florianschmidt.me",
                        "gender" to "male",
                        "hidden" to false,
                        "company" to "awesome AG",
                        "address" to mapOf(
                                "street" to "test",
                                "housenumber" to "01",
                                "city" to "Dresden",
                                "zipcode" to "01198",
                                "country" to "Germany"
                        )
                )
        ).toJsonString()

        val tokens = getTokens(this.mockMvc, participant.email, "password")

        val request = MockMvcRequestBuilders.post("/event/${event.id}/team/${team.id}/sponsoring/")
                .header("Authorization", "Bearer ${tokens.first}")
                .contentType(APPLICATION_JSON_UTF_8)
                .content(body)

        val result = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.amountPerKm").exists())
                .andExpect(jsonPath("$.limit").exists())
                .andExpect(jsonPath("$.teamId").exists())
                .andExpect(jsonPath("$.team").exists())
                .andExpect(jsonPath("$.status").value("accepted"))
                .andExpect(jsonPath("$.sponsorId").doesNotExist())
                .andExpect(jsonPath("$.unregisteredSponsor.firstname").value("Florian"))
                .andExpect(jsonPath("$.unregisteredSponsor.lastname").value("Schmidt"))
                .andExpect(jsonPath("$.unregisteredSponsor.url").value("www.florianschmidt.me"))
                .andExpect(jsonPath("$.unregisteredSponsor.gender").value("male"))
                .andExpect(jsonPath("$.unregisteredSponsor.hidden").value(false))
                .andExpect(jsonPath("$.unregisteredSponsor.company").value("awesome AG"))
                .andExpect(jsonPath("$.unregisteredSponsor.address").exists())
                .andExpect(jsonPath("$.unregisteredSponsor.address.street").value("test"))
                .andExpect(jsonPath("$.unregisteredSponsor.address.housenumber").value("01"))
                .andExpect(jsonPath("$.unregisteredSponsor.address.city").value("Dresden"))
                .andExpect(jsonPath("$.unregisteredSponsor.address.zipcode").value("01198"))
                .andExpect(jsonPath("$.unregisteredSponsor.address.country").value("Germany"))
                .andReturn().response.contentAsString

        println(result)
    }

    @Test
    fun testGetAllSponsoringsForSponsor() {
        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 0.0), 36)
        val participant = userService.create("participant@mail.de", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val team = teamService.create(participant, "name", "description", event)

        val sponsor1 = userService.create("sponsor1@mail.de", "password", { addRole(Sponsor::class) }).getRole(Sponsor::class)!!
        sponsoringService.createSponsoring(sponsor1, team, euroOf(1), euroOf(200))

        val sponsor2 = userService.create("sponsor2@mail.de", "password", { addRole(Sponsor::class) }).getRole(Sponsor::class)!!
        sponsoringService.createSponsoring(sponsor2, team, euroOf(1), euroOf(200))

        val tokens = getTokens(this.mockMvc, sponsor1.email, "password")

        val request = get("/user/${sponsor1.core.id}/sponsor/sponsoring/")
                .header("Authorization", "Bearer ${tokens.first}")

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.[0]").exists())
                .andExpect(jsonPath("$.[0].amountPerKm").exists())
                .andExpect(jsonPath("$.[0].limit").exists())
                .andExpect(jsonPath("$.[0].teamId").exists())
                .andExpect(jsonPath("$.[0].team").exists())
                .andExpect(jsonPath("$.[0].sponsorId").exists())
                .andExpect(jsonPath("$.[1]").doesNotExist())

        val unauthorized = get("/event/${event.id}/team/${team.id}/sponsoring/")
        mockMvc.perform(unauthorized).andExpect(status().isUnauthorized)
    }

    @Test
    fun testAcceptSponsoring() {
        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 0.0), 36)
        val participant = userService.create("participant@mail.de", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val sponsor = userService.create("sponsor@mail.de", "password", { addRole(Sponsor::class) }).getRole(Sponsor::class)!!
        val team = teamService.create(participant, "name", "description", event)
        val sponsoring = sponsoringService.createSponsoring(sponsor, team, euroOf(1), euroOf(200))

        val tokens = getTokens(this.mockMvc, participant.email, "password")

        val body = mapOf("status" to "accepted").toJsonString()
        val request = this.put("/event/${event.id}/team/${team.id}/sponsoring/${sponsoring.id}/status/", body)
                .header("Authorization", "Bearer ${tokens.first}")

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.amountPerKm").exists())
                .andExpect(jsonPath("$.limit").exists())
                .andExpect(jsonPath("$.teamId").exists())
                .andExpect(jsonPath("$.team").exists())
                .andExpect(jsonPath("$.sponsorId").exists())
                .andExpect(jsonPath("$.status").value("accepted"))
    }

    @Test
    fun testRejectSponsoring() {
        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 0.0), 36)
        val participant = userService.create("participant@mail.de", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val sponsor = userService.create("sponsor@mail.de", "password", { addRole(Sponsor::class) }).getRole(Sponsor::class)!!
        val team = teamService.create(participant, "name", "description", event)
        val sponsoring = sponsoringService.createSponsoring(sponsor, team, euroOf(1), euroOf(200))

        val tokens = getTokens(this.mockMvc, participant.email, "password")

        val body = mapOf("status" to "rejected").toJsonString()
        val request = this.put("/event/${event.id}/team/${team.id}/sponsoring/${sponsoring.id}/status/", body)
                .header("Authorization", "Bearer ${tokens.first}")

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.amountPerKm").exists())
                .andExpect(jsonPath("$.limit").exists())
                .andExpect(jsonPath("$.teamId").exists())
                .andExpect(jsonPath("$.team").exists())
                .andExpect(jsonPath("$.sponsorId").exists())
                .andExpect(jsonPath("$.status").value("rejected"))
    }
}
