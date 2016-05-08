package backend.controller

import backend.Integration.IntegrationTest
import backend.Integration.getTokens
import backend.Integration.toJsonString
import backend.model.misc.Coord
import backend.model.user.Participant
import backend.model.user.Sponsor
import org.javamoney.moneta.Money
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
        sponsoringService.createSponsoring(sponsor, team, Money.parse("EUR 1"), Money.parse("EUR 200"))

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
    fun testCreateSponsoring() {
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
    }
}
