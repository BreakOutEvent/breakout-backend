package backend.controller

import backend.Integration.IntegrationTest
import backend.Integration.getTokens
import backend.Integration.toJsonString
import backend.model.misc.Coord
import backend.model.user.Participant
import backend.model.user.Sponsor
import backend.util.euroOf
import org.junit.Before
import org.junit.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

class ChallengeControllerTest : IntegrationTest() {

    @Before
    override fun setUp() {
        super.setUp()
    }

    @Test
    fun testCreateChallengeWithRegisteredSponsor() {

        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 1.1), 36)
        val participant = userService.create("participant@break-out.org", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val sponsor = userService.create("sponsor@break-out.org", "password", { addRole(Sponsor::class) }).getRole(Sponsor::class)!!
        val team = teamService.create(participant, "name", "description", event)
        val tokens = getTokens(this.mockMvc, sponsor.email, "password")

        val body = mapOf(
                "amount" to 100,
                "description" to "Jump into lake titicaca naked"
        ).toJsonString()

        val request = MockMvcRequestBuilders.post("/event/${event.id}/team/${team.id}/challenge/")
                .header("Authorization", "Bearer ${tokens.first}")
                .contentType(APPLICATION_JSON_UTF_8)
                .content(body)

        mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.description").value("Jump into lake titicaca naked"))
                .andExpect(jsonPath("$.sponsorId").value(sponsor.id!!.toInt()))
                .andExpect(jsonPath("$.amount").value(100.0))
                .andExpect(jsonPath("$.unregisteredSponsor").doesNotExist())
                .andExpect(jsonPath("$.team").value(team.name))
                .andExpect(jsonPath("$.teamId").value(team.id!!.toInt()))
                .andExpect(jsonPath("$.status").value("PROPOSED"))
    }

    @Test
    fun testCreateChallengeWithUnregisteredSponsor() {
        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 1.1), 36)
        val participant = userService.create("participant@break-out.org", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        userService.create("sponsor@break-out.org", "password", { addRole(Sponsor::class) }).getRole(Sponsor::class)!!
        val team = teamService.create(participant, "name", "description", event)
        val tokens = getTokens(this.mockMvc, participant.email, "password")

        val body = mapOf(
                "amount" to 100,
                "description" to "Do something really awesome, because you know you are awesome!",
                "unregisteredSponsor" to mapOf(
                        "firstname" to "Hans",
                        "lastname" to "Meier",
                        "company" to "privat",
                        "url" to "",
                        "gender" to "male",
                        "isHidden" to "false",
                        "address" to mapOf(
                                "street" to "street",
                                "housenumber" to "123",
                                "zipcode" to "0000",
                                "city" to "City",
                                "country" to "Germany"
                        )
                )
        ).toJsonString()

        val request = MockMvcRequestBuilders.post("/event/${event.id}/team/${team.id}/challenge/")
                .header("Authorization", "Bearer ${tokens.first}")
                .contentType(APPLICATION_JSON_UTF_8)
                .content(body)

        mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.description").value("Do something really awesome, because you know you are awesome!"))
                .andExpect(jsonPath("$.sponsorId").doesNotExist())
                .andExpect(jsonPath("$.amount").value(100.0))
                .andExpect(jsonPath("$.team").value(team.name))
                .andExpect(jsonPath("$.teamId").value(team.id!!.toInt()))
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.unregisteredSponsor").exists())
                .andExpect(jsonPath("$.unregisteredSponsor.firstname").exists())
                .andExpect(jsonPath("$.unregisteredSponsor.lastname").exists())
                .andExpect(jsonPath("$.unregisteredSponsor.company").exists())
                .andExpect(jsonPath("$.unregisteredSponsor.url").exists())
                .andExpect(jsonPath("$.unregisteredSponsor.gender").exists())
                .andExpect(jsonPath("$.unregisteredSponsor.hidden").exists())
                .andExpect(jsonPath("$.unregisteredSponsor.address").exists())
    }

    @Test
    fun testAcceptChallenge() {
        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 1.1), 36)
        val participant = userService.create("participant@break-out.org", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val team = teamService.create(participant, "name", "description", event)
        val sponsor = userService.create("sponsor@break-out.org", "password", { addRole(Sponsor::class) }).getRole(Sponsor::class)!!

        setAuthenticatedUser("sponsor@break-out.org")
        val challenge = challengeService.proposeChallenge(sponsor, team, euroOf(10.0), "An awesome challenge")
        val tokens = getTokens(this.mockMvc, participant.email, "password")

        val body = mapOf(
                "status" to "accepted"
        ).toJsonString()

        val request = MockMvcRequestBuilders.put("/event/${event.id}/team/${team.id}/challenge/${challenge.id}/status/")
                .header("Authorization", "Bearer ${tokens.first}")
                .contentType(APPLICATION_JSON_UTF_8)
                .content(body)

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.description").value(challenge.description))
                .andExpect(jsonPath("$.sponsorId").value(sponsor.id!!.toInt()))
                .andExpect(jsonPath("$.amount").value(challenge.amount.numberStripped.toDouble()))
                .andExpect(jsonPath("$.unregisteredSponsor").doesNotExist())
                .andExpect(jsonPath("$.team").value(team.name))
                .andExpect(jsonPath("$.teamId").value(team.id!!.toInt()))
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
    }

    @Test
    fun testRejectChallenge() {
        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 1.1), 36)
        val participant = userService.create("participant@break-out.org", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val team = teamService.create(participant, "name", "description", event)
        val sponsor = userService.create("sponsor@break-out.org", "password", { addRole(Sponsor::class) }).getRole(Sponsor::class)!!

        setAuthenticatedUser("sponsor@break-out.org")
        val challenge = challengeService.proposeChallenge(sponsor, team, euroOf(10.0), "An awesome challenge")
        val tokens = getTokens(this.mockMvc, participant.email, "password")

        val body = mapOf(
                "status" to "rejected"
        ).toJsonString()

        val request = MockMvcRequestBuilders.put("/event/${event.id}/team/${team.id}/challenge/${challenge.id}/status/")
                .header("Authorization", "Bearer ${tokens.first}")
                .contentType(APPLICATION_JSON_UTF_8)
                .content(body)

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.description").value(challenge.description))
                .andExpect(jsonPath("$.sponsorId").value(sponsor.id!!.toInt()))
                .andExpect(jsonPath("$.amount").value(challenge.amount.numberStripped.toDouble()))
                .andExpect(jsonPath("$.unregisteredSponsor").doesNotExist())
                .andExpect(jsonPath("$.team").value(team.name))
                .andExpect(jsonPath("$.teamId").value(team.id!!.toInt()))
                .andExpect(jsonPath("$.status").value("REJECTED"))
    }
}
