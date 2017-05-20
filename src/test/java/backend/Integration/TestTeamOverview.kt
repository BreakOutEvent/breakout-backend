package backend.Integration

import backend.model.misc.Coord
import backend.model.misc.EmailAddress
import backend.model.user.Admin
import backend.model.user.Participant
import backend.testHelper.asUser
import backend.testHelper.json
import org.junit.Before
import org.junit.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime


open class TestTeamOverview : IntegrationTest() {

    @Before
    override fun setUp() {
        super.setUp()
        userService.create("admin@example.com", "pw", { addRole(Admin::class) })
    }

    @Test
    fun testShowStartedTeamsWithEmptyData() {

        // given a team has started at an event
        val event = eventService.createEvent("Testevent", LocalDateTime.now(), "Berlin", Coord(0.0), 36)

        val florian = userService.create("florian@example.com", "pw", {
            addRole(Participant::class).apply {
                firstname = "Florian"
                lastname = "Schmidt"
                emergencynumber = "123"
                phonenumber = "456"
            }
        }).getRole(Participant::class)!!

        val max = userService.create("max@example.com", "pw", {
            addRole(Participant::class).apply {
                firstname = "Max"
                lastname = "Kattner"
                emergencynumber = "789"
                phonenumber = "012"
            }
        }).getRole(Participant::class)!!

        val team = teamService.create(florian, "name", "description", event)

        setAuthenticatedUser(florian.email)
        teamService.invite(EmailAddress(max.email), team)

        setAuthenticatedUser(max.email)
        teamService.join(max, team)

        // when requesting the team overview
        val request = get("/teamoverview/")
                .asUser(this.mockMvc, "admin@example.com", "pw")

        // then it contains this team with "empty" default data
        val res = mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.[0].teamId").value(team.id))
                .andExpect(jsonPath("$.[0].teamId").value(team.id))
                .andExpect(jsonPath("$.[0].members").isArray)
                .andExpect(jsonPath("$.[0].members.[0].id").isNumber)
                .andExpect(jsonPath("$.[0].members.[0].firstname").isString)
                .andExpect(jsonPath("$.[0].members.[0].emergencyPhone").isString)
                .andExpect(jsonPath("$.[0].members.[0].contactPhone").isString)
                .andExpect(jsonPath("$.[0].members.[1].id").isNumber)
                .andExpect(jsonPath("$.[0].members.[1].firstname").isString)
                .andExpect(jsonPath("$.[0].members.[1].emergencyPhone").isString)
                .andExpect(jsonPath("$.[0].members.[1].contactPhone").isString)
                .andExpect(jsonPath("$.[0].event.id").isNumber)
                .andExpect(jsonPath("$.[0].event.name").value("Testevent"))
                .andExpect(jsonPath("$.[0].lastPosting").doesNotExist())
                .andExpect(jsonPath("$.[0].lastLocation").doesNotExist())
                .andExpect(jsonPath("$.[0].lastContactWithHeadquarters").doesNotExist())
    }

    @Test
    fun testShowLocationsInTeamOverview() {

        // given a team has started at an event
        val event = eventService.createEvent("Testevent", LocalDateTime.now(), "Berlin", Coord(0.0), 36)

        val florian = userService.create("florian@example.com", "pw", {
            addRole(Participant::class).apply {
                firstname = "Florian"
                lastname = "Schmidt"
                emergencynumber = "123"
                phonenumber = "456"
            }
        }).getRole(Participant::class)!!

        val max = userService.create("max@example.com", "pw", {
            addRole(Participant::class).apply {
                firstname = "Max"
                lastname = "Kattner"
                emergencynumber = "789"
                phonenumber = "012"
            }
        }).getRole(Participant::class)!!

        val team = teamService.create(florian, "name", "description", event)

        setAuthenticatedUser(florian.email)
        teamService.invite(EmailAddress(max.email), team)

        setAuthenticatedUser(max.email)
        teamService.join(max, team)

        locationService.create(Coord(3.3, 1.1), max, LocalDateTime.now(), false)
        val location = locationService.create(Coord(2.2, 3.3), max, LocalDateTime.now(), false)

        // when requesting the team overview
        val request = get("/teamoverview/")
                .asUser(this.mockMvc, "admin@example.com", "pw")

        // then it contains this team with "empty" default data
        val res = mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.[0].teamId").value(team.id))
                .andExpect(jsonPath("$.[0].teamId").value(team.id))
                .andExpect(jsonPath("$.[0].members").isArray)
                .andExpect(jsonPath("$.[0].members.[0].id").isNumber)
                .andExpect(jsonPath("$.[0].members.[0].firstname").isString)
                .andExpect(jsonPath("$.[0].members.[0].emergencyPhone").isString)
                .andExpect(jsonPath("$.[0].members.[0].contactPhone").isString)
                .andExpect(jsonPath("$.[0].members.[1].id").isNumber)
                .andExpect(jsonPath("$.[0].members.[1].firstname").isString)
                .andExpect(jsonPath("$.[0].members.[1].emergencyPhone").isString)
                .andExpect(jsonPath("$.[0].members.[1].contactPhone").isString)
                .andExpect(jsonPath("$.[0].event.id").isNumber)
                .andExpect(jsonPath("$.[0].event.name").value("Testevent"))
                .andExpect(jsonPath("$.[0].lastLocation.coord.latitude").value(2.2))
                .andExpect(jsonPath("$.[0].lastLocation.coord.longitude").value(3.3))
                .andExpect(jsonPath("$.[0].lastLocation.id").value(location.id))
                .andExpect(jsonPath("$.[0].lastPosting").doesNotExist())
                .andExpect(jsonPath("$.[0].lastContactWithHeadquarters").doesNotExist())
    }

    @Test
    fun testShowPostingsInTeamOverview() {

        // given a team has started at an event
        val event = eventService.createEvent("Testevent", LocalDateTime.now(), "Berlin", Coord(0.0), 36)

        val florian = userService.create("florian@example.com", "pw", {
            addRole(Participant::class).apply {
                firstname = "Florian"
                lastname = "Schmidt"
                emergencynumber = "123"
                phonenumber = "456"
            }
        }).getRole(Participant::class)!!

        val max = userService.create("max@example.com", "pw", {
            addRole(Participant::class).apply {
                firstname = "Max"
                lastname = "Kattner"
                emergencynumber = "789"
                phonenumber = "012"
            }
        }).getRole(Participant::class)!!

        val team = teamService.create(florian, "name", "description", event)

        setAuthenticatedUser(florian.email)
        teamService.invite(EmailAddress(max.email), team)

        setAuthenticatedUser(max.email)
        teamService.join(max, team)

        postingService.createPosting(max, "", listOf(), null, LocalDateTime.now())
        // when requesting the team overview
        val request = get("/teamoverview/")
                .asUser(this.mockMvc, "admin@example.com", "pw")

        // then it contains this team with "empty" default data
        val res = mockMvc.perform(request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.[0].teamId").value(team.id))
                .andExpect(jsonPath("$.[0].teamId").value(team.id))
                .andExpect(jsonPath("$.[0].members").isArray)
                .andExpect(jsonPath("$.[0].members.[0].id").isNumber)
                .andExpect(jsonPath("$.[0].members.[0].firstname").isString)
                .andExpect(jsonPath("$.[0].members.[0].emergencyPhone").isString)
                .andExpect(jsonPath("$.[0].members.[0].contactPhone").isString)
                .andExpect(jsonPath("$.[0].members.[1].id").isNumber)
                .andExpect(jsonPath("$.[0].members.[1].firstname").isString)
                .andExpect(jsonPath("$.[0].members.[1].emergencyPhone").isString)
                .andExpect(jsonPath("$.[0].members.[1].contactPhone").isString)
                .andExpect(jsonPath("$.[0].event.id").isNumber)
                .andExpect(jsonPath("$.[0].event.name").value("Testevent"))
                .andExpect(jsonPath("$.[0].lastPosting.timestamp").isNumber)
                .andExpect(jsonPath("$.[0].lastPosting.id").isNumber)
                .andExpect(jsonPath("$.[0].lastContactWithHeadquarters").doesNotExist())
    }

    @Test
    fun testAdminCanAddNotesToTeamOverview() {
        // given a team has started at an event
        val event = eventService.createEvent("Testevent", LocalDateTime.now(), "Berlin", Coord(0.0), 36)

        val florian = userService.create("florian@example.com", "pw", {
            addRole(Participant::class).apply {
                firstname = "Florian"
                lastname = "Schmidt"
                emergencynumber = "123"
                phonenumber = "456"
            }
        }).getRole(Participant::class)!!

        val max = userService.create("max@example.com", "pw", {
            addRole(Participant::class).apply {
                firstname = "Max"
                lastname = "Kattner"
                emergencynumber = "789"
                phonenumber = "012"
            }
        }).getRole(Participant::class)!!

        val team = teamService.create(florian, "name", "description", event)

        setAuthenticatedUser(florian.email)
        teamService.invite(EmailAddress(max.email), team)

        setAuthenticatedUser(max.email)
        teamService.join(max, team)

        // when adding a comment to a team
        val request = MockMvcRequestBuilders.post("/teamoverview/${team.id}/lastContactWithHeadquarters/")
                .asUser(this.mockMvc, "admin@example.com", "pw")
                .json(mapOf(
                        "comment" to "Alles gut, weitermachen"
                ))

        // then it succeeds
        mockMvc.perform(request)
                .andExpect(status().isCreated)


        // and the overview for this team contains timestamp and comment
        val check = get("/teamoverview/")
                .asUser(this.mockMvc, "admin@example.com", "pw")

        // then it contains this team with "empty" default data
        mockMvc.perform(check)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.[0].teamId").value(team.id))
                .andExpect(jsonPath("$.[0].lastContactWithHeadquarters.timestamp").isNumber)
                .andExpect(jsonPath("$.[0].lastContactWithHeadquarters.comment").value("----Alles gut, weitermachen"))
    }
}
