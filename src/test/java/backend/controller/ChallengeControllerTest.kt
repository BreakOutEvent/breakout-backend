package backend.controller

import backend.Integration.IntegrationTest
import backend.Integration.toJsonString
import backend.model.event.Event
import backend.model.event.Team
import backend.model.misc.Coord
import backend.model.user.Participant
import backend.model.user.Sponsor
import backend.testHelper.asUser
import backend.testHelper.json
import backend.util.euroOf
import org.junit.Before
import org.junit.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime
import java.time.ZoneOffset

class ChallengeControllerTest : IntegrationTest() {

    private lateinit var event: Event
    private lateinit var participant: Participant
    private lateinit var team: Team
    private lateinit var sponsor: Sponsor

    @Before
    override fun setUp() {
        super.setUp()
        this.event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 1.1), 36)
        this.participant = userService.create("participant@break-out.org", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        this.team = teamService.create(participant, "name", "description", event)
        this.sponsor = userService.create("sponsor@break-out.org", "password", { addRole(Sponsor::class) }).getRole(Sponsor::class)!!
    }

    @Test
    fun testCreateChallengeWithRegisteredSponsor() {

        val body = mapOf(
                "amount" to 100,
                "description" to "Jump into lake titicaca naked"
        ).toJsonString()

        val request = post("/event/${event.id}/team/${team.id}/challenge/")
                .json(body)
                .asUser(this.mockMvc, sponsor.email, "password")

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
        )

        val request = post("/event/${event.id}/team/${team.id}/challenge/")
                .json(body)
                .asUser(this.mockMvc, participant.email, "password")

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

        setAuthenticatedUser("sponsor@break-out.org")
        val challenge = challengeService.proposeChallenge(sponsor, team, euroOf(10.0), "An awesome challenge")

        val body = mapOf("status" to "accepted")

        val request = put("/event/${event.id}/team/${team.id}/challenge/${challenge.id}/status/")
                .json(body)
                .asUser(this.mockMvc, participant.email, "password")

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

        setAuthenticatedUser("sponsor@break-out.org")
        val challenge = challengeService.proposeChallenge(sponsor, team, euroOf(10.0), "An awesome challenge")

        val body = mapOf("status" to "rejected")

        val request = put("/event/${event.id}/team/${team.id}/challenge/${challenge.id}/status/")
                .json(body)
                .asUser(this.mockMvc, participant.email, "password")

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

    @Test
    fun testFulfillChallenge() {

        val posting = postingService.createPosting(participant, "text", null, null, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
        setAuthenticatedUser("sponsor@break-out.org")
        val challenge = challengeService.proposeChallenge(sponsor, team, euroOf(10.0), "An awesome challenge")

        val body = mapOf(
                "status" to "with_proof",
                "postingId" to posting.id
        )

        val request = put("/event/${event.id}/team/${team.id}/challenge/${challenge.id}/status/")
                .json(body)
                .asUser(this.mockMvc, participant.email, "password")

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.description").value(challenge.description))
                .andExpect(jsonPath("$.sponsorId").value(sponsor.id!!.toInt()))
                .andExpect(jsonPath("$.amount").value(challenge.amount.numberStripped.toDouble()))
                .andExpect(jsonPath("$.unregisteredSponsor").doesNotExist())
                .andExpect(jsonPath("$.team").value(team.name))
                .andExpect(jsonPath("$.teamId").value(team.id!!.toInt()))
                .andExpect(jsonPath("$.status").value("WITH_PROOF"))
    }
}
