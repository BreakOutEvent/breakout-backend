package backend.controller

import backend.Integration.IntegrationTest
import backend.Integration.toJsonString
import backend.model.event.Event
import backend.model.event.Team
import backend.model.misc.Coord
import backend.model.sponsoring.UnregisteredSponsor
import backend.model.user.Address
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

    @Test
    fun testShowAllChallenges() {

        setAuthenticatedUser("sponsor@break-out.org")
        challengeService.proposeChallenge(sponsor, team, euroOf(10.0), "An awesome challenge")

        val unregisteredSponsor = UnregisteredSponsor(
                firstname = "Hans",
                lastname = "Wurst",
                company = "privat",
                url = "test",
                isHidden = false,
                gender = "male",
                address = Address(
                        street = "Straße",
                        housenumber = "4",
                        city = "City",
                        zipcode = "01111",
                        country = "Germany"
                )
        )
        setAuthenticatedUser(participant.email)
        challengeService.proposeChallenge(unregisteredSponsor, team, euroOf(10.0), "An awesome challenge")

        val request = get("/event/${event.id}/team/${team.id}/challenge/")

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.[0]").exists())
                .andExpect(jsonPath("$.[0].description").exists())
                .andExpect(jsonPath("$.[0].amount").exists())
                .andExpect(jsonPath("$.[0].sponsorId").exists())
                .andExpect(jsonPath("$.[0].sponsorIsHidden").value(false))
                .andExpect(jsonPath("$.[0].unregisteredSponsor").doesNotExist())
                .andExpect(jsonPath("$.[0].team").exists())
                .andExpect(jsonPath("$.[0].teamId").exists())
                .andExpect(jsonPath("$.[0].status").exists())
                .andExpect(jsonPath("$.[1].description").exists())
                .andExpect(jsonPath("$.[1].amount").exists())
                .andExpect(jsonPath("$.[1].sponsorId").doesNotExist())
                .andExpect(jsonPath("$.[0].sponsorIsHidden").value(false))
                .andExpect(jsonPath("$.[1].unregisteredSponsor").exists())
                .andExpect(jsonPath("$.[1].unregisteredSponsor.firstname").exists())
                .andExpect(jsonPath("$.[1].unregisteredSponsor.lastname").exists())
                .andExpect(jsonPath("$.[1].unregisteredSponsor.company").exists())
                .andExpect(jsonPath("$.[1].unregisteredSponsor.url").exists())
                .andExpect(jsonPath("$.[1].unregisteredSponsor.gender").exists())
                .andExpect(jsonPath("$.[1].unregisteredSponsor.address").doesNotExist())
                .andExpect(jsonPath("$.[1].team").exists())
                .andExpect(jsonPath("$.[1].teamId").exists())
                .andExpect(jsonPath("$.[1].status").exists())
    }

    @Test
    fun createChallengePreferUnregisteredSponsorOverRoleSponsor() {

        participant.addRole(Sponsor::class)
        userService.save(participant)

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
    }

    @Test
    fun dontShowDataForHiddenSponsor() {

        val sponsor1 = userService.create("sponsor1@break-out.org", "password", {
            addRole(Sponsor::class).isHidden = true
        }).getRole(Sponsor::class)!!

        setAuthenticatedUser("sponsor1@break-out.org")

        challengeService.proposeChallenge(sponsor1, team, euroOf(10.0), "An awesome challenge")

        val unregisteredSponsor = UnregisteredSponsor(
                firstname = "Hans",
                lastname = "Wurst",
                company = "privat",
                url = "test",
                isHidden = true,
                gender = "male",
                address = Address(
                        street = "Straße",
                        housenumber = "4",
                        city = "City",
                        zipcode = "01111",
                        country = "Germany"
                )
        )
        setAuthenticatedUser(participant.email)
        challengeService.proposeChallenge(unregisteredSponsor, team, euroOf(10.0), "An awesome challenge")

        val request = get("/event/${event.id}/team/${team.id}/challenge/")

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.[0]").exists())
                .andExpect(jsonPath("$.[0].description").exists())
                .andExpect(jsonPath("$.[0].amount").exists())
                .andExpect(jsonPath("$.[0].sponsorIsHidden").value(true))
                .andExpect(jsonPath("$.[0].sponsorId").doesNotExist())
                .andExpect(jsonPath("$.[0].unregisteredSponsor").doesNotExist())
                .andExpect(jsonPath("$.[0].team").exists())
                .andExpect(jsonPath("$.[0].teamId").exists())
                .andExpect(jsonPath("$.[0].status").exists())
                .andExpect(jsonPath("$.[1].description").exists())
                .andExpect(jsonPath("$.[1].amount").exists())
                .andExpect(jsonPath("$.[1].sponsorId").doesNotExist())
                .andExpect(jsonPath("$.[1].unregisteredSponsor").doesNotExist())
                .andExpect(jsonPath("$.[1].sponsorIsHidden").value(true))
                .andExpect(jsonPath("$.[1].team").exists())
                .andExpect(jsonPath("$.[1].teamId").exists())
                .andExpect(jsonPath("$.[1].status").exists())
    }
}