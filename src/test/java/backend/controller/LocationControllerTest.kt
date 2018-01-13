package backend.controller

import backend.Integration.IntegrationTest
import backend.model.event.Event
import backend.model.event.Team
import backend.model.misc.Coord
import backend.model.misc.EmailAddress
import backend.model.user.Participant
import backend.services.Feature
import backend.testHelper.asUser
import backend.testHelper.json
import org.hamcrest.Matchers.hasSize
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

class LocationControllerTest : IntegrationTest() {

    private lateinit var firstUser: Participant
    private lateinit var thirdUser: Participant
    private lateinit var fifthUser: Participant

    private lateinit var munichEvent: Event
    private lateinit var berlinEvent: Event

    private lateinit var firstTeam: Team
    private lateinit var secondTeam: Team
    private lateinit var thirdTeam: Team

    @Before
    override fun setUp() {
        super.setUp()

        munichEvent = eventService.createEvent("Event 1", LocalDateTime.now(), "munich", Coord(1.0, 1.0), 36)
        berlinEvent = eventService.createEvent("Event 2", LocalDateTime.now(), "berlin", Coord(1.1, 0.0), 36)

        firstUser = userService.create("test1@break-out.org", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val secondUser = userService.create("test2@break-out.org", "password", { addRole(Participant::class) }).getRole(Participant::class)!!

        thirdUser = userService.create("test3@break-out.org", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val fourthUser = userService.create("test4@break-out.org", "password", { addRole(Participant::class) }).getRole(Participant::class)!!

        fifthUser = userService.create("test5@break-out.org", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val sixthUser = userService.create("test6@break-out.org", "password", { addRole(Participant::class) }).getRole(Participant::class)!!

        firstTeam = teamService.create(firstUser, "team awesome", "description", munichEvent, null)
        setAuthenticatedUser(firstUser.email)
        teamService.invite(EmailAddress(secondUser.email), firstTeam)
        teamService.join(secondUser, firstTeam)

        secondTeam = teamService.create(thirdUser, "team not awesome", "description", munichEvent, null)
        setAuthenticatedUser(thirdUser.email)
        teamService.invite(EmailAddress(fourthUser.email), secondTeam)
        teamService.join(fourthUser, secondTeam)

        thirdTeam = teamService.create(fifthUser, "team shit", "description", berlinEvent, null)
        setAuthenticatedUser(fifthUser.email)
        teamService.invite(EmailAddress(sixthUser.email), thirdTeam)
        teamService.join(sixthUser, thirdTeam)
    }

    @Ignore //locations are filtered during event
    @Test
    fun testGetAllLocationsForEvent() {
        locationService.create(Coord(1.0, 1.0), firstUser, LocalDateTime.now())
        locationService.create(Coord(1.0, 1.0), thirdUser, LocalDateTime.now())
        locationService.create(Coord(1.0, 1.0), fifthUser, LocalDateTime.now()) // This one should not be found!

        val request = get("/event/${munichEvent.id}/location/")

        val resp = mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath<MutableCollection<out Any>>("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].event").exists())
                .andExpect(jsonPath("$[0].description").exists())
                .andExpect(jsonPath("$[0].hasStarted").exists())
                .andExpect(jsonPath("$[0].members").exists())
                .andExpect(jsonPath("$[0].profilePic").exists())
                .andExpect(jsonPath("$[0].locations").isArray)
                .andExpect(jsonPath("$[0].locations[0].id").exists())
                .andExpect(jsonPath("$[0].locations[0].latitude").exists())
                .andExpect(jsonPath("$[0].locations[0].longitude").exists())
                .andExpect(jsonPath("$[0].locations[0].distance").exists())
                .andExpect(jsonPath("$[0].locations[0].date").exists())
                .andExpect(jsonPath("$[0].locations[0].locationData").exists())
                .andExpect(jsonPath("$[1].id").exists())
                .andExpect(jsonPath("$[1].name").exists())
                .andExpect(jsonPath("$[1].event").exists())
                .andExpect(jsonPath("$[1].description").exists())
                .andExpect(jsonPath("$[1].hasStarted").exists())
                .andExpect(jsonPath("$[1].members").exists())
                .andExpect(jsonPath("$[1].profilePic").exists())
                .andExpect(jsonPath("$[1].locations").isArray)
                .andExpect(jsonPath("$[1].locations[0].id").exists())
                .andExpect(jsonPath("$[1].locations[0].latitude").exists())
                .andExpect(jsonPath("$[1].locations[0].longitude").exists())
                .andExpect(jsonPath("$[1].locations[0].distance").exists())
                .andExpect(jsonPath("$[1].locations[0].date").exists())
                .andExpect(jsonPath("$[1].locations[0].locationData").exists())
                .andReturn().response.contentAsString

        println(resp)
    }

    @Ignore //locations are filtered during event
    @Test
    fun testGetAllLocationsForEventAndTeam() {
        locationService.create(Coord(1.0, 1.0), firstUser, LocalDateTime.now())
        locationService.create(Coord(1.0, 1.0), thirdUser, LocalDateTime.now())
        locationService.create(Coord(1.0, 1.0), fifthUser, LocalDateTime.now()) // This one should not be found!

        val request = get("/event/${munichEvent.id}/team/${firstTeam.id}/location/")

        mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath<MutableCollection<out Any>>("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].latitude").exists())
                .andExpect(jsonPath("$[0].longitude").exists())
                .andExpect(jsonPath("$[0].team").exists())
                .andExpect(jsonPath("$[0].teamId").exists())
                .andExpect(jsonPath("$[0].eventId").exists())
                .andExpect(jsonPath("$[0].event").exists())
                .andExpect(jsonPath("$[0].locationData").exists())
                .andExpect(jsonPath("$[0].locationData.COUNTRY").doesNotExist())

    }

    @Test
    fun testCreateLocation() {

        val data = mapOf(
                "latitude" to 1.0,
                "longitude" to 1.0,
                "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        )

        val request = post("/event/${munichEvent.id}/team/${firstTeam.id}/location/")
                .asUser(mockMvc, firstUser.email, "password")
                .json(data)

        val resp = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.latitude").exists()) //TODO: Check equality, not only existence
                .andExpect(jsonPath("$.longitude").exists())
                .andExpect(jsonPath("$.team").exists())
                .andExpect(jsonPath("$.teamId").exists())
                .andExpect(jsonPath("$.eventId").exists())
                .andExpect(jsonPath("$.event").exists())
                .andExpect(jsonPath("$.duringEvent").exists())
                .andExpect(jsonPath("$.locationData").exists())
                .andExpect(jsonPath("$.locationData.COUNTRY").doesNotExist())
                .andReturn().response.contentAsString

        println(resp)
    }


    @Test
    fun testCreateLocationDuringEvent() {

        val timeEvent = LocalDateTime.of(2016, 6, 3, 9, 0, 0)
        val timeLocation = LocalDateTime.of(2016, 6, 3, 9, 1, 0)

        val thisEvent = eventService.createEvent("Event", timeEvent, "Test", Coord(1.0, 1.0), 36)
        val thisUser = userService.create("testduring@break-out.org", "password", { addRole(Participant::class) }).getRole(Participant::class)!!

        // TODO: Refactor this
        val thisTeam = teamService.create(thisUser, "team awesome", "description", thisEvent, null)
        thisTeam.hasStarted = true
        teamService.save(thisTeam)
        setAuthenticatedUser(thisUser.email)

        val feature = Feature("event.isNow", true)
        featureRepository.save(feature)

        val data = mapOf(
                "latitude" to 1.0,
                "longitude" to 1.0,
                "date" to timeLocation.atZone(ZoneId.systemDefault()).toInstant().epochSecond
        )

        val request = post("/event/${thisEvent.id}/team/${thisTeam.id}/location/")
                .asUser(mockMvc, thisUser.email, "password")
                .json(data)

        val resp = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.latitude").exists())
                .andExpect(jsonPath("$.longitude").exists())
                .andExpect(jsonPath("$.team").exists())
                .andExpect(jsonPath("$.teamId").exists())
                .andExpect(jsonPath("$.eventId").exists())
                .andExpect(jsonPath("$.event").exists())
                .andExpect(jsonPath("$.locationData").exists())
                .andExpect(jsonPath("$.locationData.COUNTRY").doesNotExist())
                .andExpect(jsonPath("$.duringEvent").value(true))
                .andReturn().response.contentAsString

        println("RESP: $resp")
    }

    @Test
    fun testCreateLocationNotDuringEventInFront() {

        val timeEvent = LocalDateTime.of(2016, 6, 3, 9, 0, 0)
        val timeLocation = LocalDateTime.of(2016, 6, 3, 8, 59, 0)

        val thisEvent = eventService.createEvent("Event", timeEvent, "Test", Coord(1.0, 1.0), 36)
        val thisUser = userService.create("testduring@break-out.org", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val thisTeam = teamService.create(thisUser, "team awesome", "description", thisEvent, null)
        setAuthenticatedUser(thisUser.email)

        val feature = Feature("event.isNow", false)
        featureRepository.save(feature)

        val data = mapOf(
                "latitude" to 1.0,
                "longitude" to 1.0,
                "date" to timeLocation.atZone(ZoneId.systemDefault()).toInstant().epochSecond
        )

        val request = post("/event/${thisEvent.id}/team/${thisTeam.id}/location/")
                .asUser(mockMvc, thisUser.email, "password")
                .json(data)

        val resp = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.latitude").exists())
                .andExpect(jsonPath("$.longitude").exists())
                .andExpect(jsonPath("$.team").exists())
                .andExpect(jsonPath("$.teamId").exists())
                .andExpect(jsonPath("$.eventId").exists())
                .andExpect(jsonPath("$.event").exists())
                .andExpect(jsonPath("$.locationData").exists())
                .andExpect(jsonPath("$.locationData.COUNTRY").doesNotExist())
                .andExpect(jsonPath("$.duringEvent").value(false))
                .andReturn().response.contentAsString

        println(resp)
    }

    @Test
    fun testCreateLocationNotDuringEventAfter() {

        val timeEvent = LocalDateTime.of(2016, 6, 3, 9, 0, 0)
        val timeLocation = LocalDateTime.of(2016, 6, 4, 21, 1, 0)

        val thisEvent = eventService.createEvent("Event", timeEvent, "Test", Coord(1.0, 1.0), 36)
        val thisUser = userService.create("testduring@break-out.org", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val thisTeam = teamService.create(thisUser, "team awesome", "description", thisEvent, null)

        val feature = Feature("event.isNow", false)
        featureRepository.save(feature)

        setAuthenticatedUser(thisUser.email)

        val data = mapOf(
                "latitude" to 1.0,
                "longitude" to 1.0,
                "date" to timeLocation.atZone(ZoneId.systemDefault()).toInstant().epochSecond
        )

        val request = post("/event/${thisEvent.id}/team/${thisTeam.id}/location/")
                .asUser(mockMvc, thisUser.email, "password")
                .json(data)

        val resp = mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.latitude").exists())
                .andExpect(jsonPath("$.longitude").exists())
                .andExpect(jsonPath("$.team").exists())
                .andExpect(jsonPath("$.teamId").exists())
                .andExpect(jsonPath("$.eventId").exists())
                .andExpect(jsonPath("$.event").exists())
                .andExpect(jsonPath("$.locationData").exists())
                .andExpect(jsonPath("$.locationData.COUNTRY").doesNotExist())
                .andExpect(jsonPath("$.duringEvent").value(false))
                .andReturn().response.contentAsString

        println(resp)
    }

    @Test
    fun testCreateMultipleLocation() {

        val data = listOf(
                mapOf(
                        "latitude" to 1.0,
                        "longitude" to 1.0,
                        "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)),
                mapOf(
                        "latitude" to 2.2,
                        "longitude" to 3.3,
                        "date" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
        )

        val request = post("/event/${munichEvent.id}/team/${firstTeam.id}/location/multiple/")
                .asUser(mockMvc, firstUser.email, "password")
                .json(data)

        mockMvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath<MutableCollection<out Any>>("$", hasSize(2)))
                .andExpect(jsonPath("$[0].latitude").exists())
                .andExpect(jsonPath("$[0].longitude").exists())
                .andExpect(jsonPath("$[0].team").exists())
                .andExpect(jsonPath("$[0].teamId").exists())
                .andExpect(jsonPath("$[0].eventId").exists())
                .andExpect(jsonPath("$[0].event").exists())
                .andExpect(jsonPath("$[0].locationData").exists())
                .andExpect(jsonPath("$[0].locationData.COUNTRY").doesNotExist())
                .andExpect(jsonPath("$[1].team").exists())
                .andExpect(jsonPath("$[1].teamId").exists())
                .andExpect(jsonPath("$[1].eventId").exists())
                .andExpect(jsonPath("$[1].event").exists())
                .andExpect(jsonPath("$[1].locationData").exists())
                .andExpect(jsonPath("$[1].locationData.COUNTRY").doesNotExist())

    }
}
