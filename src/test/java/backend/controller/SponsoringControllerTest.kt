package backend.controller

import backend.Integration.IntegrationTest
import backend.model.misc.Coord
import backend.model.sponsoring.UnregisteredSponsor
import backend.model.user.Address
import backend.model.user.Participant
import backend.model.user.Sponsor
import backend.testHelper.asUser
import backend.testHelper.json
import backend.util.euroOf
import org.junit.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

class SponsoringControllerTest : IntegrationTest() {

    @Test
    fun testGetAllSponsoringsAuthenticated() {

        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 0.0), 36)
        val participant = userService.create("participant@mail.de", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val sponsor = userService.create("sponsor@mail.de", "password", { addRole(Sponsor::class) }).getRole(Sponsor::class)!!
        val team = teamService.create(participant, "name", "description", event)
        val sponsoring = sponsoringService.createSponsoring(sponsor, team, euroOf(1), euroOf(200))

        val request = get("/event/${event.id}/team/${team.id}/sponsoring/")
                .asUser(mockMvc, participant.email, "password")

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.[0]").exists())
                .andExpect(jsonPath("$.[0].amountPerKm").exists())
                .andExpect(jsonPath("$.[0].limit").exists())
                .andExpect(jsonPath("$.[0].teamId").exists())
                .andExpect(jsonPath("$.[0].team").exists())
                .andExpect(jsonPath("$.[0].sponsorId").exists())
                .andExpect(jsonPath("$.[0].userId").exists())
                .andExpect(jsonPath("$.[0].id").value(sponsoring.id!!.toInt()))
                .andExpect(jsonPath("$.[0].eventId").value(event.id!!.toInt()))
    }

    @Test
    fun testGetAllSponsoringsUnauthenticated() {

        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 0.0), 36)
        val participant = userService.create("participant@mail.de", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val sponsor = userService.create("sponsor@mail.de", "password", { addRole(Sponsor::class) }).getRole(Sponsor::class)!!
        val team = teamService.create(participant, "name", "description", event)
        val sponsoring = sponsoringService.createSponsoring(sponsor, team, euroOf(1), euroOf(200))

        val request = get("/event/${event.id}/team/${team.id}/sponsoring/")

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.[0]").exists())
                .andExpect(jsonPath("$.[0].amountPerKm").exists())
                .andExpect(jsonPath("$.[0].limit").exists())
                .andExpect(jsonPath("$.[0].teamId").exists())
                .andExpect(jsonPath("$.[0].team").exists())
                .andExpect(jsonPath("$.[0].sponsorId").exists())
                .andExpect(jsonPath("$.[0].userId").exists())
                .andExpect(jsonPath("$.[0].id").value(sponsoring.id!!.toInt()))
                .andExpect(jsonPath("$.[0].eventId").value(event.id!!.toInt()))
    }

    @Test
    fun testGetAllSponsoringsUnauthenticatedUnregisteredSponsor() {
        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 0.0), 36)
        val participant = userService.create("participant@mail.de", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val sponsor = UnregisteredSponsor(
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
        val team = teamService.create(participant, "name", "description", event)

        setAuthenticatedUser(participant.email)
        val sponsoring = sponsoringService.createSponsoringWithOfflineSponsor(team, euroOf(1), euroOf(200), sponsor)

        val request = get("/event/${event.id}/team/${team.id}/sponsoring/")

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.[0]").exists())
                .andExpect(jsonPath("$.[0].amountPerKm").exists())
                .andExpect(jsonPath("$.[0].limit").exists())
                .andExpect(jsonPath("$.[0].teamId").exists())
                .andExpect(jsonPath("$.[0].team").exists())
                .andExpect(jsonPath("$.[0].sponsorId").doesNotExist())
                .andExpect(jsonPath("$.[0].userId").doesNotExist())
                .andExpect(jsonPath("$.[0].id").value(sponsoring.id!!.toInt()))
                .andExpect(jsonPath("$.[0].eventId").value(event.id!!.toInt()))
                .andExpect(jsonPath("$.[0].unregisteredSponsor").exists())
                .andExpect(jsonPath("$.[0].unregisteredSponsor.address").doesNotExist())
    }

    @Test
    fun testCreateSponsoringWithHiddenSponsor() {

        //Set-Up
        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 0.0), 36)
        val participant = userService.create("participant@mail.de", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
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

        val registeredSponsor = userService.create("sponsor@mail.de", "password", {
            addRole(Sponsor::class).isHidden = true
        }).getRole(Sponsor::class)!!

        setAuthenticatedUser(registeredSponsor.email)
        val team = teamService.create(participant, "name", "description", event)

        val sponsoring1 = sponsoringService.createSponsoring(registeredSponsor, team, euroOf(1), euroOf(200))
        setAuthenticatedUser(participant.email)
        val sponsoring2 = sponsoringService.createSponsoringWithOfflineSponsor(team, euroOf(1), euroOf(200), unregisteredSponsor)

        // Test & Assertions
        val request = get("/event/${event.id}/team/${team.id}/sponsoring/")

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.[0]").exists())
                .andExpect(jsonPath("$.[0].sponsorIsHidden").value(true))
                .andExpect(jsonPath("$.[0].amountPerKm").exists())
                .andExpect(jsonPath("$.[0].limit").exists())
                .andExpect(jsonPath("$.[0].teamId").exists())
                .andExpect(jsonPath("$.[0].team").exists())
                .andExpect(jsonPath("$.[0].sponsoring").doesNotExist())
                .andExpect(jsonPath("$.[0].id").value(sponsoring1.id!!.toInt()))
                .andExpect(jsonPath("$.[0].eventId").value(event.id!!.toInt()))
                .andExpect(jsonPath("$.[0].unregisteredSponsor").doesNotExist())
                .andExpect(jsonPath("$.[0].contract").doesNotExist())
                .andExpect(jsonPath("$.[1].amountPerKm").exists())
                .andExpect(jsonPath("$.[1].sponsorIsHidden").value(true))
                .andExpect(jsonPath("$.[1].limit").exists())
                .andExpect(jsonPath("$.[1].teamId").exists())
                .andExpect(jsonPath("$.[1].team").exists())
                .andExpect(jsonPath("$.[1].sponsorId").doesNotExist())
                .andExpect(jsonPath("$.[1].userId").doesNotExist())
                .andExpect(jsonPath("$.[1].id").value(sponsoring2.id!!.toInt()))
                .andExpect(jsonPath("$.[1].eventId").value(event.id!!.toInt()))
                .andExpect(jsonPath("$.[1].unregisteredSponsor").doesNotExist())
                .andExpect(jsonPath("$.[1].contract").doesNotExist())
    }

    @Test
    fun testCreateSponsoringWithRegisteredSponsor() {
        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 0.0), 36)
        val participant = userService.create("participant@mail.de", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val sponsor = userService.create("sponsor@mail.de", "password", { addRole(Sponsor::class) }).getRole(Sponsor::class)!!
        val team = teamService.create(participant, "name", "description", event)

        val body = mapOf("amountPerKm" to 1.0, "limit" to 200)

        val request = post("/event/${event.id}/team/${team.id}/sponsoring/")
                .asUser(mockMvc, sponsor.email, "password")
                .json(body)

        mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.amountPerKm").exists())
                .andExpect(jsonPath("$.limit").exists())
                .andExpect(jsonPath("$.teamId").exists())
                .andExpect(jsonPath("$.team").exists())
                .andExpect(jsonPath("$.sponsorId").exists())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.status").value("PROPOSED"))
                .andExpect(jsonPath("$.unregisteredSponsor").doesNotExist())
                .andExpect(jsonPath("$.contract.type").exists())
                .andExpect(jsonPath("$.contract.id").exists())
                .andExpect(jsonPath("$.contract.uploadToken").exists())
    }

    @Test
    fun testCreateSponsoringWithRegisteredSponsorDoubleAmount() {
        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 0.0), 36)
        val participant = userService.create("participant@mail.de", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val sponsor = userService.create("sponsor@mail.de", "password", { addRole(Sponsor::class) }).getRole(Sponsor::class)!!
        val team = teamService.create(participant, "name", "description", event)

        val body = mapOf("amountPerKm" to 0.35, "limit" to 200)

        val request = post("/event/${event.id}/team/${team.id}/sponsoring/")
                .asUser(mockMvc, sponsor.email, "password")
                .json(body)

        mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.amountPerKm").exists())
                .andExpect(jsonPath("$.limit").exists())
                .andExpect(jsonPath("$.teamId").exists())
                .andExpect(jsonPath("$.team").exists())
                .andExpect(jsonPath("$.sponsorId").exists())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.status").value("PROPOSED"))
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
                        "email" to "sponsor@example.com",
                        "company" to "awesome AG",
                        "address" to mapOf(
                                "street" to "test",
                                "housenumber" to "01",
                                "city" to "Dresden",
                                "zipcode" to "01198",
                                "country" to "Germany"
                        )
                )
        )

        val request = post("/event/${event.id}/team/${team.id}/sponsoring/")
                .asUser(mockMvc, participant.email, "password")
                .json(body)

        val result = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.amountPerKm").exists())
                .andExpect(jsonPath("$.limit").exists())
                .andExpect(jsonPath("$.teamId").exists())
                .andExpect(jsonPath("$.team").exists())
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.sponsorId").doesNotExist())
                .andExpect(jsonPath("$.userId").doesNotExist())
                .andExpect(jsonPath("$.contract.type").exists())
                .andExpect(jsonPath("$.contract.id").exists())
                .andExpect(jsonPath("$.contract.uploadToken").exists())
                .andExpect(jsonPath("$.unregisteredSponsor.firstname").value("Florian"))
                .andExpect(jsonPath("$.unregisteredSponsor.lastname").value("Schmidt"))
                .andExpect(jsonPath("$.unregisteredSponsor.url").value("www.florianschmidt.me"))
                .andExpect(jsonPath("$.unregisteredSponsor.gender").value("male"))
                .andExpect(jsonPath("$.unregisteredSponsor.hidden").value(false))
                .andExpect(jsonPath("$.unregisteredSponsor.company").value("awesome AG"))
                .andExpect(jsonPath("$.unregisteredSponsor.email").value("sponsor@example.com"))
                .andExpect(jsonPath("$.unregisteredSponsor.address").exists())
                .andExpect(jsonPath("$.unregisteredSponsor.address.street").value("test"))
                .andExpect(jsonPath("$.unregisteredSponsor.address.housenumber").value("01"))
                .andExpect(jsonPath("$.unregisteredSponsor.address.city").value("Dresden"))
                .andExpect(jsonPath("$.unregisteredSponsor.address.zipcode").value("01198"))
                .andExpect(jsonPath("$.unregisteredSponsor.address.country").value("Germany"))
                .andReturn().response.contentAsString

        println(result)
    }

    /**
     * Whenever a user is both an sponsor and a participant and wants to add
     * a sponsoring for it's current team by providing data for unregisteredSponsor,
     * this data should be preferred instead of using the users role sponsor for the sponsoring
     *
     * See: https://github.com/BreakOutEvent/breakout-backend/issues/134
     */
    @Test
    fun testCreateSponsoringAsUserWhoIsBothSponsorAndParticipant() {
        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 0.0), 36)
        val user = userService.create("participant@mail.de", "password", {
            addRole(Participant::class)
            addRole(Sponsor::class)
        })
        val team = teamService.create(user.getRole(Participant::class)!!, "name", "description", event)

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
        )

        val request = post("/event/${event.id}/team/${team.id}/sponsoring/")
                .asUser(mockMvc, user.email, "password")
                .json(body)

        mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.unregisteredSponsor").exists())
                .andExpect(jsonPath("$.sponsorId").doesNotExist())
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

        val request = get("/user/${sponsor1.account.id}/sponsor/sponsoring/")
                .asUser(mockMvc, sponsor1.email, "password")

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.[0]").exists())
                .andExpect(jsonPath("$.[0].amountPerKm").exists())
                .andExpect(jsonPath("$.[0].limit").exists())
                .andExpect(jsonPath("$.[0].teamId").exists())
                .andExpect(jsonPath("$.[0].team").exists())
                .andExpect(jsonPath("$.[0].sponsorId").exists())
                .andExpect(jsonPath("$.[0].userId").exists())
                .andExpect(jsonPath("$.[1]").doesNotExist())
    }

    @Test
    fun testAcceptSponsoring() {
        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 0.0), 36)
        val participant = userService.create("participant@mail.de", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val sponsor = userService.create("sponsor@mail.de", "password", { addRole(Sponsor::class) }).getRole(Sponsor::class)!!
        val team = teamService.create(participant, "name", "description", event)
        val sponsoring = sponsoringService.createSponsoring(sponsor, team, euroOf(1), euroOf(200))

        val body = mapOf("status" to "ACCEPTED")

        val request = put("/event/${event.id}/team/${team.id}/sponsoring/${sponsoring.id}/status/")
                .asUser(mockMvc, participant.email, "password")
                .json(body)

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.amountPerKm").exists())
                .andExpect(jsonPath("$.limit").exists())
                .andExpect(jsonPath("$.teamId").exists())
                .andExpect(jsonPath("$.team").exists())
                .andExpect(jsonPath("$.sponsorId").exists())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
    }

    @Test
    fun testRejectSponsoring() {
        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 0.0), 36)
        val participant = userService.create("participant@mail.de", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val sponsor = userService.create("sponsor@mail.de", "password", { addRole(Sponsor::class) }).getRole(Sponsor::class)!!
        val team = teamService.create(participant, "name", "description", event)
        val sponsoring = sponsoringService.createSponsoring(sponsor, team, euroOf(1), euroOf(200))

        val body = mapOf("status" to "rejected")

        val request = put("/event/${event.id}/team/${team.id}/sponsoring/${sponsoring.id}/status/")
                .asUser(mockMvc, participant.email, "password")
                .json(body)

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.amountPerKm").exists())
                .andExpect(jsonPath("$.limit").exists())
                .andExpect(jsonPath("$.teamId").exists())
                .andExpect(jsonPath("$.team").exists())
                .andExpect(jsonPath("$.sponsorId").exists())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.status").value("REJECTED"))
    }

    @Test
    fun testWithdrawSponsoringForUnregisteredSponsor() {
        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 0.0), 36)
        val participant = userService.create("participant@mail.de", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val team = teamService.create(participant, "name", "description", event)
        val sponsor = UnregisteredSponsor("", "", "", "", "", address = Address("", "", "", "", ""))
        setAuthenticatedUser(participant.email)
        val sponsoring = sponsoringService.createSponsoringWithOfflineSponsor(team, euroOf(1), euroOf(200), sponsor)

        val body = mapOf("status" to "withdrawn")

        val request = put("/event/${event.id}/team/${team.id}/sponsoring/${sponsoring.id}/status/")
                .asUser(mockMvc, participant.email, "password")
                .json(body)

        val unauthRequest = put("/event/${event.id}/team/${team.id}/sponsoring/${sponsoring.id}/status/")
                .json(body)

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.status").value("WITHDRAWN"))

        mockMvc.perform(unauthRequest)
                .andExpect(status().isUnauthorized)
    }

    @Test
    fun testWithdrawSponsoringForRegisteredSponsor() {

        val event = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0, 0.0), 36)
        val participant = userService.create("participant@mail.de", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val team = teamService.create(participant, "name", "description", event)
        val sponsor = userService.create("sponsor@break-out.org", "password", { addRole(Sponsor::class) }).getRole(Sponsor::class)!!

        setAuthenticatedUser(sponsor.email)
        val sponsoring = sponsoringService.createSponsoring(sponsor, team, euroOf(1), euroOf(200))

        val body = mapOf("status" to "withdrawn")

        val request = put("/event/${event.id}/team/${team.id}/sponsoring/${sponsoring.id}/status/")
                .asUser(mockMvc, sponsor.email, "password")
                .json(body)

        val unauthRequest = put("/event/${event.id}/team/${team.id}/sponsoring/${sponsoring.id}/status/")
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
