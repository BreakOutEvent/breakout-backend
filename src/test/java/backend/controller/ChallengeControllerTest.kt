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

class ChallengeControllerTest : IntegrationTest() {

    private lateinit var event: Event
    private lateinit var participant: Participant
    private lateinit var team: Team
    private lateinit var sponsor: Sponsor

    @Before
    override fun setUp() {
        super.setUp()
        this.event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 1.1), 36)
        val user = userService.create("participant@break-out.org", "password")
        user.addRole(Participant::class)
        this.participant = userService.save(user).getRole(Participant::class)!!
        this.team = teamService.create(participant, "name", "description", event, null)
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
                .andExpect(jsonPath("$.userId").value(sponsor.account.id!!.toInt()))
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
                        "isHidden" to "false",
                        "email" to "sponsor@example.com",
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
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.eventId").value(team.event.id!!.toInt()))
                .andExpect(jsonPath("$.description").value("Do something really awesome, because you know you are awesome!"))
                .andExpect(jsonPath("$.sponsorId").doesNotExist())
                .andExpect(jsonPath("$.userId").doesNotExist())
                .andExpect(jsonPath("$.amount").value(100.0))
                .andExpect(jsonPath("$.team").value(team.name))
                .andExpect(jsonPath("$.teamId").value(team.id!!.toInt()))
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.unregisteredSponsor").exists())
                .andExpect(jsonPath("$.unregisteredSponsor.email").exists())
                .andExpect(jsonPath("$.unregisteredSponsor.firstname").exists())
                .andExpect(jsonPath("$.unregisteredSponsor.lastname").exists())
                .andExpect(jsonPath("$.unregisteredSponsor.company").exists())
                .andExpect(jsonPath("$.unregisteredSponsor.url").exists())
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
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.eventId").value(team.event.id!!.toInt()))
                .andExpect(jsonPath("$.description").value(challenge.description))
                .andExpect(jsonPath("$.sponsorId").value(sponsor.id!!.toInt()))
                .andExpect(jsonPath("$.userId").value(sponsor.account.id!!.toInt()))
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

        val body = mapOf("status" to "REJECTED")

        val request = put("/event/${event.id}/team/${team.id}/challenge/${challenge.id}/status/")
                .json(body)
                .asUser(this.mockMvc, participant.email, "password")

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.eventId").value(team.event.id!!.toInt()))
                .andExpect(jsonPath("$.description").value(challenge.description))
                .andExpect(jsonPath("$.sponsorId").value(sponsor.id!!.toInt()))
                .andExpect(jsonPath("$.userId").value(sponsor.account.id!!.toInt()))
                .andExpect(jsonPath("$.amount").value(challenge.amount.numberStripped.toDouble()))
                .andExpect(jsonPath("$.unregisteredSponsor").doesNotExist())
                .andExpect(jsonPath("$.team").value(team.name))
                .andExpect(jsonPath("$.teamId").value(team.id!!.toInt()))
                .andExpect(jsonPath("$.status").value("REJECTED"))
    }

    @Test
    fun testFulfillChallenge() {

        val posting = postingService.createPosting(participant, "text", null, null, LocalDateTime.now())
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
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.eventId").value(team.event.id!!.toInt()))
                .andExpect(jsonPath("$.description").value(challenge.description))
                .andExpect(jsonPath("$.sponsorId").value(sponsor.id!!.toInt()))
                .andExpect(jsonPath("$.userId").value(sponsor.account.id!!.toInt()))
                .andExpect(jsonPath("$.amount").value(challenge.amount.numberStripped.toDouble()))
                .andExpect(jsonPath("$.unregisteredSponsor").doesNotExist())
                .andExpect(jsonPath("$.team").value(team.name))
                .andExpect(jsonPath("$.teamId").value(team.id!!.toInt()))
                .andExpect(jsonPath("$.status").value("WITH_PROOF"))

        val requestPosting = get("/posting/${posting.id}/")

        mockMvc.perform(requestPosting)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.text").exists())
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.user").exists())
                .andExpect(jsonPath("$.proves.id").exists())
                .andExpect(jsonPath("$.proves.description").value(challenge.description))
                .andExpect(jsonPath("$.proves.amount").value(challenge.amount.numberStripped.toDouble()))
                .andExpect(jsonPath("$.proves.status").value("WITH_PROOF"))
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
                .andExpect(jsonPath("$.[0].id").exists())
                .andExpect(jsonPath("$.[0].eventId").exists())
                .andExpect(jsonPath("$.[0].description").exists())
                .andExpect(jsonPath("$.[0].amount").exists())
                .andExpect(jsonPath("$.[0].sponsorId").exists())
                .andExpect(jsonPath("$.[0].userId").exists())
                .andExpect(jsonPath("$.[0].sponsorIsHidden").value(false))
                .andExpect(jsonPath("$.[0].unregisteredSponsor").doesNotExist())
                .andExpect(jsonPath("$.[0].team").exists())
                .andExpect(jsonPath("$.[0].teamId").exists())
                .andExpect(jsonPath("$.[0].status").exists())
                .andExpect(jsonPath("$.[0].contract.uploadToken").doesNotExist())
                .andExpect(jsonPath("$.[1].description").exists())
                .andExpect(jsonPath("$.[1].amount").exists())
                .andExpect(jsonPath("$.[1].sponsorId").doesNotExist())
                .andExpect(jsonPath("$.[1].userId").doesNotExist())
                .andExpect(jsonPath("$.[0].sponsorIsHidden").value(false))
                .andExpect(jsonPath("$.[1].unregisteredSponsor").exists())
                .andExpect(jsonPath("$.[1].unregisteredSponsor.firstname").exists())
                .andExpect(jsonPath("$.[1].unregisteredSponsor.lastname").exists())
                .andExpect(jsonPath("$.[1].unregisteredSponsor.company").exists())
                .andExpect(jsonPath("$.[1].unregisteredSponsor.url").exists())
                .andExpect(jsonPath("$.[1].unregisteredSponsor.address").doesNotExist())
                .andExpect(jsonPath("$.[1].team").exists())
                .andExpect(jsonPath("$.[1].teamId").exists())
                .andExpect(jsonPath("$.[1].status").exists())
                .andExpect(jsonPath("$.[1].id").exists())
                .andExpect(jsonPath("$.[1].eventId").exists())
                .andExpect(jsonPath("$.[1].contract.uploadToken").doesNotExist())
    }

    @Test
    fun getAllChallengesForSponsor() {
        val participant2 = userService.create("participant2@break-out.org", "password", { addRole(Participant::class) })
        val team2 = teamService.create(participant2.getRole(Participant::class)!!, "", "", event, null)

        setAuthenticatedUser(sponsor.email)
        challengeService.proposeChallenge(sponsor, team, euroOf(10.0), "description")
        challengeService.proposeChallenge(sponsor, team2, euroOf(10.0), "description")

        val unauthRequest = get("/user/${sponsor.account.id}/sponsor/challenge/")
        val authRequest = get("/user/${sponsor.account.id}/sponsor/challenge/").asUser(this.mockMvc, sponsor.email, "password")

        mockMvc.perform(unauthRequest).andExpect(status().isUnauthorized)

        mockMvc.perform(authRequest)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.[0]").exists())
                .andExpect(jsonPath("$.[1]").exists())
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
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.eventId").value(team.event.id!!.toInt()))
                .andExpect(jsonPath("$.description").value("Do something really awesome, because you know you are awesome!"))
                .andExpect(jsonPath("$.sponsorId").doesNotExist())
                .andExpect(jsonPath("$.userId").doesNotExist())
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
                .andExpect(jsonPath("$.[0].userId").doesNotExist())
                .andExpect(jsonPath("$.[0].unregisteredSponsor").doesNotExist())
                .andExpect(jsonPath("$.[0].team").exists())
                .andExpect(jsonPath("$.[0].teamId").exists())
                .andExpect(jsonPath("$.[0].status").exists())
                .andExpect(jsonPath("$.[0].contract").doesNotExist())
                .andExpect(jsonPath("$.[1].description").exists())
                .andExpect(jsonPath("$.[1].amount").exists())
                .andExpect(jsonPath("$.[1].sponsorId").doesNotExist())
                .andExpect(jsonPath("$.[1].userId").doesNotExist())
                .andExpect(jsonPath("$.[1].unregisteredSponsor").doesNotExist())
                .andExpect(jsonPath("$.[1].sponsorIsHidden").value(true))
                .andExpect(jsonPath("$.[1].team").exists())
                .andExpect(jsonPath("$.[1].teamId").exists())
                .andExpect(jsonPath("$.[1].status").exists())
                .andExpect(jsonPath("$.[1].contract").doesNotExist())
    }

    @Test
    fun testWithdrawChallengeForUnregisteredSponsor() {
        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 0.0), 36)
        val participant = userService.create("participant@mail.de", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val team = teamService.create(participant, "name", "description", event, null)
        val sponsor = UnregisteredSponsor("", "", "", "", "", address = Address("", "", "", "", ""))
        setAuthenticatedUser(participant.email)
        val challenge = challengeService.proposeChallenge(sponsor, team, euroOf(200), "desc")

        val body = mapOf("status" to "withdrawn")

        val request = put("/event/${event.id}/team/${team.id}/challenge/${challenge.id}/status/")
                .asUser(mockMvc, participant.email, "password")
                .json(body)

        val unauthRequest = put("/event/${event.id}/team/${team.id}/challenge/${challenge.id}/status/")
                .json(body)

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.status").value("WITHDRAWN"))

        mockMvc.perform(unauthRequest)
                .andExpect(status().isUnauthorized)
    }

    @Test
    fun testWithdrawChallengeForRegisteredSponsor() {

        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 0.0), 36)
        val participant = userService.create("participant@mail.de", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val team = teamService.create(participant, "name", "description", event, null)
        val sponsor = userService.create("challenger@break-out.org", "password", { addRole(Sponsor::class) }).getRole(Sponsor::class)!!

        setAuthenticatedUser(sponsor.email)
        val challenge = challengeService.proposeChallenge(sponsor, team, euroOf(200), "desc")

        val body = mapOf("status" to "withdrawn")

        val request = put("/event/${event.id}/team/${team.id}/challenge/${challenge.id}/status/")
                .asUser(mockMvc, sponsor.email, "password")
                .json(body)

        val unauthRequest = put("/event/${event.id}/team/${team.id}/challenge/${challenge.id}/status/")
                .asUser(mockMvc, participant.email, "password")
                .json(body)

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.status").value("WITHDRAWN"))

        mockMvc.perform(unauthRequest)
                .andExpect(status().isForbidden) //TODO: Check why forbidden / unauthorized difference
    }
}
