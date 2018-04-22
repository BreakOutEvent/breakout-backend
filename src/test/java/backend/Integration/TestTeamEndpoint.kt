package backend.Integration

import backend.model.event.Event
import backend.model.event.Team
import backend.model.location.Location
import backend.model.misc.Coord
import backend.model.posting.Posting
import backend.model.user.Participant
import backend.model.user.User
import backend.services.ConfigurationService
import backend.testHelper.json
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.hasSize
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertFails

@Transactional
open class TestTeamEndpoint : IntegrationTest() {

    @Autowired
    lateinit var configurationService: ConfigurationService
    lateinit var JWT_SECRET: String
    lateinit var event: Event
    lateinit var team: Team
    lateinit var creatorCredentials: Credentials
    lateinit var creator: User
    lateinit var inviteeCredentials: Credentials
    lateinit var invitee: User

    @Before
    override fun setUp() {
        super.setUp()

        this.JWT_SECRET = configurationService.getRequired("org.breakout.api.jwt_secret")
        event = eventService.createEvent(
                title = "Breakout München",
                date = LocalDateTime.now(),
                city = "Munich",
                startingLocation = Coord(0.0, 0.0),
                duration = 36)

        creatorCredentials = createUser(this.mockMvc, userService = userService)
        inviteeCredentials = createUser(this.mockMvc, email = "invitee@mail.com", userService = userService)
        makeUserParticipant(creatorCredentials)
        failMakeUserParticipantMissingData(creatorCredentials)
        makeUserParticipant(inviteeCredentials)
        creator = userRepository.findOne(creatorCredentials.id.toLong()).getRole(Participant::class)!!
        invitee = userRepository.findOne(inviteeCredentials.id.toLong())
        team = teamService.create(creator as Participant, "name", "description", event, null)

        val fakeLocationData = mapOf("COUNTRY" to "Germany")

        val firstLocation = Location(Coord(1.0, 1.0), creator.getRole(Participant::class)!!, LocalDateTime.now(), fakeLocationData)
        val secondLocation = Location(Coord(1.2, 2.0), creator.getRole(Participant::class)!!, LocalDateTime.now(), fakeLocationData)

        postingService.save(Posting("test", LocalDateTime.now(), firstLocation, creator.account, null))
        postingService.save(Posting("test", LocalDateTime.now(), secondLocation, creator.account, null))

    }

    @Test
    fun eventEndedMail() {
        teamService.sendEmailsToTeamsWhenEventHasEnded()
    }

    @Test
    fun testCreateTeam() {

        val body = mapOf("name" to "Team awesome", "description" to "Our team is awesome", "profilePic" to mapOf("type" to "image", "url" to "url")).toJsonString()

        val request = post("/event/${event.id}/team/")
                // TODO: Stop using inviteeCredentials just because creator already is part of a team because of setUp()
                // TODO: This is just a workaround and the tests should be refactored to be structured properly
                .header("Authorization", "Bearer ${inviteeCredentials.accessToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)

        val response = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.profilePic.type").exists())
                .andExpect(jsonPath("$.profilePic.id").exists())
                .andExpect(jsonPath("$.profilePic.url").exists())
                .andExpect(jsonPath("$.profilePic.type").value("IMAGE"))
                .andExpect(jsonPath("$.event").value(event.id!!.toInt()))
                .andExpect(jsonPath("$.name").value("Team awesome"))
                .andExpect(jsonPath("$.description").value("Our team is awesome"))
                .andExpect(jsonPath("$.members").isArray)
                .andExpect(jsonPath("$.invoiceId").exists())
                .andExpect(jsonPath<MutableCollection<out Any>>("$.members", hasSize(1)))
                .andReturn().response.contentAsString

        print(response)
    }

    @Test
    fun givenAUserIsInATeamAtAnEventWhenHeWantsToCreateAnotherTeamThenItFails() {
        val testUserCredentials = createUser(this.mockMvc, "test@example.com", "pw", this.userService)
        makeUserParticipant(testUserCredentials)

        createTeam(testUserCredentials, "name", "description", event.id!!)

        assertFails {
            createTeam(testUserCredentials, "name", "description", event.id!!)
        }
    }

    private fun createTeam(credentials: Credentials, name: String, description: String, eventId: Long): Long {
        val body = mapOf(
                "name" to name,
                "description" to description,
                "profilePic" to mapOf("type" to "image", "url" to "url")
        )

        val request = post("/event/$eventId/team/")
                .header("Authorization", "Bearer ${credentials.accessToken}")
                .json(body)

        val res = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.profilePic.type").exists())
                .andExpect(jsonPath("$.profilePic.id").exists())
                .andExpect(jsonPath("$.profilePic.url").exists())
                .andExpect(jsonPath("$.profilePic.type").value("IMAGE"))
                .andExpect(jsonPath("$.event").value(event.id!!.toInt()))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.description").value(description))
                .andExpect(jsonPath("$.members").isArray)
                .andExpect(jsonPath("$.invoiceId").exists())
                .andExpect(jsonPath<MutableCollection<out Any>>("$.members", hasSize(1)))
                .andReturn().response.contentAsString

        val jsonRes: Map<String, Any> = ObjectMapper()
                .readerFor(Map::class.java)
                .readValue(res)

        return (jsonRes["id"] as? Int)?.toLong() ?: throw Exception("Can't parse id ${jsonRes["id"]} to long")

    }

    @Test
    fun failToCreateTeamIfUserIsNoParticipant() {
        val body = mapOf("name" to "Team awesome", "description" to "This team is awesome").toJsonString()

        val request = post("event/${event.id}/team")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)

        mockMvc.perform(request).andExpect(status().isNotFound)
    }

    @Test
    fun testGetTeamById() {
        val request = MockMvcRequestBuilders
                .request(HttpMethod.GET, "/event/${event.id}/team/${team.id}/")
                .contentType(MediaType.APPLICATION_JSON)

        val response = mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.event").exists())
                .andExpect(jsonPath("$.invoiceId").exists())
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.distance").exists())
                .andExpect(jsonPath("$.donateSum.sponsorSum").exists())
                .andExpect(jsonPath("$.donateSum.fullSum").exists())
                .andExpect(jsonPath("$.members").exists())
                .andExpect(jsonPath("$.members[0].id").exists())
                .andExpect(jsonPath("$.members[0].firstname").exists())
                .andExpect(jsonPath("$.members[0].lastname").exists())
                .andExpect(jsonPath("$.members[0].gender").exists())
                .andExpect(jsonPath("$.members[0].profilePic").exists())
                .andExpect(jsonPath("$.members[0].roles").exists())
                .andReturn().response.contentAsString

        println(response)
    }


    @Test
    fun testEditTeam() {

        val body = mapOf("name" to "Team megaAwesome", "description" to "Our team is super awesome", "profilePic" to mapOf("type" to "image", "url" to "url")).toJsonString()

        val request = put("/event/${event.id}/team/${team.id}/")
                .header("Authorization", "Bearer ${creatorCredentials.accessToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)

        val response = mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.profilePic").exists())
                .andExpect(jsonPath("$.profilePic.type").exists())
                .andExpect(jsonPath("$.profilePic.id").exists())
                .andExpect(jsonPath("$.profilePic.url").exists())
                .andExpect(jsonPath("$.profilePic.type").value("IMAGE"))
                .andExpect(jsonPath("$.event").value(event.id!!.toInt()))
                .andExpect(jsonPath("$.name").value("Team megaAwesome"))
                .andExpect(jsonPath("$.description").value("Our team is super awesome"))
                .andExpect(jsonPath("$.members").isArray)
                .andExpect(jsonPath("$.invoiceId").exists())
                .andReturn().response.contentAsString

        print(response)
    }


    @Test
    fun testEditTeamNotMemberFails() {

        val body = mapOf("name" to "Team megaAwesome", "description" to "Our team is super awesome").toJsonString()

        //invitee not yet Member
        val request = put("/event/${event.id}/team/${team.id}/")
                .header("Authorization", "Bearer ${inviteeCredentials.accessToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)

        val response = mockMvc.perform(request)
                .andExpect(status().isUnauthorized)
                .andReturn().response.contentAsString

        print(response)
    }

    @Test
    fun testGetTeamsByEvent() {
        val request = MockMvcRequestBuilders
                .request(HttpMethod.GET, "/event/${event.id}/team/")
                .contentType(MediaType.APPLICATION_JSON)

        val response = mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].event").exists())
                .andExpect(jsonPath("$[0].invoiceId").exists())
                .andExpect(jsonPath("$[0].description").exists())
                .andExpect(jsonPath("$[0].members").exists())
                .andExpect(jsonPath("$[0].members[0].id").exists())
                .andExpect(jsonPath("$[0].members[0].firstname").exists())
                .andExpect(jsonPath("$[0].members[0].lastname").exists())
                .andExpect(jsonPath("$[0].members[0].profilePic").exists())
                .andExpect(jsonPath("$[0].members[0].roles").exists())
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    fun testGetTeamPostingsById() {
        val request = MockMvcRequestBuilders
                .request(HttpMethod.GET, "/event/${event.id}/team/${team.id}/posting/")
                .contentType(MediaType.APPLICATION_JSON)

        val response = mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[1]").exists())
                .andExpect(jsonPath("$[2]").doesNotExist())
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    fun testGetTeamDistanceById() {
        //TODO check actual values with dummy data
        val request = MockMvcRequestBuilders
                .request(HttpMethod.GET, "/event/${event.id}/team/${team.id}/distance/")
                .contentType(MediaType.APPLICATION_JSON)

        val response = mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.distance").exists())
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    fun testGetTeamDonateSumById() {
        //TODO check actual values with dummy data
        val request = MockMvcRequestBuilders
                .request(HttpMethod.GET, "/event/${event.id}/team/${team.id}/donatesum/")
                .contentType(MediaType.APPLICATION_JSON)

        val response = mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.sponsorSum").exists())
                .andExpect(jsonPath("$.fullSum").exists())
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    fun testInviteUser() {
        val body = mapOf("email" to invitee.email).toJsonString()

        val request = post("/event/${event.id}/team/${team.id}/invitation/")
                .header("Authorization", "Bearer ${creatorCredentials.accessToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)

        val response = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.status").exists())
                .andReturn().response.contentAsString

        println(response)
    }

    @Test
    fun joinTeam() {

        // TODO: Is this a good practice? How to do integration tests...
        testInviteUser()

        val body = mapOf("email" to invitee.email).toJsonString()
        val joinRequest = post("/event/${event.id}/team/${team.id}/member/")
                .header("Authorization", "Bearer ${inviteeCredentials.accessToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)

        // Join team
        val response = mockMvc.perform(joinRequest)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.event").exists())
                .andExpect(jsonPath("$.invoiceId").exists())
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.members").exists())
                .andReturn().response.contentAsString

        println(response)

        val request = MockMvcRequestBuilders
                .request(HttpMethod.GET, "/event/${event.id}/team/${team.id}/")
                .contentType(MediaType.APPLICATION_JSON)

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.event").exists())
                .andExpect(jsonPath("$.invoiceId").exists())
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.members").isArray)
                .andExpect(jsonPath("$.members[0].id").exists())
                .andExpect(jsonPath("$.members[0].firstname").exists())
                .andExpect(jsonPath("$.members[0].lastname").exists())
                .andExpect(jsonPath("$.members[0].profilePic").exists())
                .andExpect(jsonPath("$.members[0].roles").exists())
                .andExpect(jsonPath("$.members[1].id").exists())
                .andExpect(jsonPath("$.members[1].firstname").exists())
                .andExpect(jsonPath("$.members[1].lastname").exists())
                .andExpect(jsonPath("$.members[1].profilePic").exists())
                .andExpect(jsonPath("$.members[1].roles").exists())
                .andReturn().response.contentAsString

    }

    private fun failMakeUserParticipantMissingData(credentials: Credentials) {

        val date = LocalDate.now().toString()

        // Update user with role participant
        val json = mapOf(
                "firstname" to "Florian",
                "lastname" to "Schmidt",
                "gender" to "Male",
                "blocked" to false,
                "participant" to mapOf(
                        "birthdate" to date
                )
        ).toJsonString()

        val request = put("/user/${credentials.id}/")
                .header("Authorization", "Bearer ${credentials.accessToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)

        mockMvc.perform(request)
                .andExpect(status().isBadRequest)
    }


    private fun makeUserParticipant(credentials: Credentials) {

        val date = LocalDate.now().toString()

        // Update user with role participant
        val json = mapOf(
                "firstname" to "Florian",
                "lastname" to "Schmidt",
                "gender" to "Male",
                "blocked" to false,
                "participant" to mapOf(
                        "tshirtsize" to "XL",
                        "hometown" to "Dresden",
                        "birthdate" to date,
                        "phonenumber" to "01234567890",
                        "emergencynumber" to "0987654321"
                )
        ).toJsonString()

        val request = put("/user/${credentials.id}/")
                .header("Authorization", "Bearer ${credentials.accessToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(credentials.id))
                .andExpect(jsonPath("$.firstname").value("Florian"))
                .andExpect(jsonPath("$.lastname").value("Schmidt"))
                .andExpect(jsonPath("$.gender").value("Male"))
                .andExpect(jsonPath("$.blocked").value(false))
                .andExpect(jsonPath("$.participant").exists())
                .andExpect(jsonPath("$.participant.tshirtsize").value("XL"))
                .andExpect(jsonPath("$.participant.hometown").value("Dresden"))
                .andExpect(jsonPath("$.participant.birthdate").value(date))
                .andExpect(jsonPath("$.participant.phonenumber").value("01234567890"))
                .andExpect(jsonPath("$.participant.emergencynumber").value("0987654321"))
    }

}
