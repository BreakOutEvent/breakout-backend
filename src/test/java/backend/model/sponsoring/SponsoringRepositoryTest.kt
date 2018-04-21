package backend.model.sponsoring

import backend.Integration.IntegrationTest
import backend.model.event.Event
import backend.model.event.Team
import backend.model.event.TeamService
import backend.model.misc.Coord
import backend.model.misc.EmailAddress
import backend.model.user.Address
import backend.model.user.Participant
import backend.model.user.Sponsor
import backend.model.user.UserService
import backend.util.euroOf
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import java.time.LocalDateTime
import kotlin.test.assertEquals


class SponsoringRepositoryTest : IntegrationTest() {

    private lateinit var firstRegisteredSponsor: Sponsor
    private lateinit var secondRegisteredSponsor: Sponsor

    private lateinit var firstTeamAtFirstEvent: Team
    private lateinit var secondTeamAtFirstEvent: Team
    private lateinit var firstTeamAtSecondEvent: Team
    private lateinit var secondTeamAtSecondEvent: Team

    private lateinit var firstEvent: Event
    private lateinit var secondEvent: Event

    @Before
    override fun setUp() {
        super.setUp()

        firstEvent = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0), 36)

        secondEvent = eventService.createEvent("another title", LocalDateTime.now(), "city", Coord(0.0), 36)

        firstRegisteredSponsor = userService.create("sponsor-1@example.com", "pw", {
            addRole(Sponsor::class).apply {
                firstname = "Mike"
                lastname = "Mustermann"
                address = Address("street", "hnr", "city", "country", "zipcode")
            }
        }).getRole(Sponsor::class)!!

        secondRegisteredSponsor = userService.create("sponsor-2@example.com", "pw", {
            addRole(Sponsor::class).apply {
                firstname = "Michaels"
                lastname = "Musterfrau"
                address = Address("street", "hnr", "city", "country", "zipcode")
            }
        }).getRole(Sponsor::class)!!

        firstTeamAtFirstEvent = TeamBuilder.createWith(teamService, userService, userDetailsService) {
            name = "Geiles Team"
            description = "Wir sind die geilsten"
            this.event = firstEvent
            creator {
                firstname = "Florian"
                lastname = "Schmidt"
                email = "teilnehmer-1@example.com"
                password = ""
            }
            invited {
                firstname = "Max"
                lastname = "Mustermann"
                email = "teilnehmer-2@example.com"
                password = "pw"
            }
        }.build()

        secondTeamAtFirstEvent = TeamBuilder.createWith(teamService, userService, userDetailsService) {
            name = "Noch ein geiles Team"
            description = "Wir sind auch nicht schlecht"
            this.event = firstEvent
            creator {
                firstname = "Fritz"
                lastname = "Fleißig"
                email = "teilnehmer-3@example.com"
                password = "pw"
            }
            invited {
                firstname = "Hans"
                lastname = "Hurtig"
                email = "teilnehmer-4@example.com"
                password = "pw"
            }
        }.build()

        firstTeamAtSecondEvent = TeamBuilder.createWith(teamService, userService, userDetailsService) {
            name = "Noch ein geiles Team"
            description = "Wir sind auch nicht schlecht"
            this.event = secondEvent
            creator {
                firstname = "Fritz"
                lastname = "Fleißig"
                email = "teilnehmer-5@example.com"
                password = "pw"
            }
            invited {
                firstname = "Hans"
                lastname = "Hurtig"
                email = "teilnehmer-6@example.com"
                password = "pw"
            }
        }.build()

        secondTeamAtSecondEvent = TeamBuilder.createWith(teamService, userService, userDetailsService) {
            name = "Noch ein geiles Team"
            description = "Wir sind auch nicht schlecht"
            this.event = secondEvent
            creator {
                firstname = "Fritz"
                lastname = "Fleißig"
                email = "teilnehmer-7@example.com"
                password = "pw"
            }
            invited {
                firstname = "Hans"
                lastname = "Hurtig"
                email = "teilnehmer-8@example.com"
                password = "pw"
            }
        }.build()
    }

    @Test
    fun testFindAllRegisteredSponsorsWithChallengesAtEvent() {

        setAuthenticatedUser(firstRegisteredSponsor.email)
        challengeService.proposeChallenge(firstRegisteredSponsor, firstTeamAtFirstEvent, euroOf(1), "")
        challengeService.proposeChallenge(firstRegisteredSponsor, firstTeamAtSecondEvent, euroOf(1), "")

        setAuthenticatedUser(secondRegisteredSponsor.email)
        challengeService.proposeChallenge(secondRegisteredSponsor, secondTeamAtSecondEvent, euroOf(1), "")

        val fromFirst = challengeRepository.findAllRegisteredSponsorsWithChallengesAtEvent(firstEvent.id!!)
        val fromSecond = challengeRepository.findAllRegisteredSponsorsWithChallengesAtEvent(secondEvent.id!!)

        assertEquals(1, fromFirst.count())
        assertEquals(2, fromSecond.count())
    }

    @Test
    fun testFindAllRegisteredSponsorsWhenOneSponsorHasChallengesForMultipleTeamsAtTheSameEvent() {
        setAuthenticatedUser(firstRegisteredSponsor.email)

        challengeService.proposeChallenge(firstRegisteredSponsor, firstTeamAtFirstEvent, euroOf(0.0), "")
        challengeService.proposeChallenge(firstRegisteredSponsor, secondTeamAtFirstEvent, euroOf(0.0), "")

        val fromFirst = challengeRepository.findAllRegisteredSponsorsWithChallengesAtEvent(firstEvent.id!!)

        assertEquals(2, fromFirst.count())
    }

    @Test
    fun testFindAllRegisteredSponsorsWhenOneSponsorHasChallengesForMultipleTeamsAtMultipleEvents() {

        setAuthenticatedUser(firstRegisteredSponsor.email)

        challengeService.proposeChallenge(firstRegisteredSponsor, firstTeamAtFirstEvent, euroOf(0.0), "")
        challengeService.proposeChallenge(firstRegisteredSponsor, firstTeamAtSecondEvent, euroOf(0.0), "")

        val fromFirst = challengeRepository.findAllRegisteredSponsorsWithChallengesAtEvent(firstEvent.id!!)
        val fromSecond = challengeRepository.findAllRegisteredSponsorsWithChallengesAtEvent(secondEvent.id!!)

        assertEquals(1, fromFirst.count())
        assertEquals(1, fromSecond.count())
    }

    @Test
    fun testFindAllUnregisteredSponsorsWithChallengesAtEvent() {

        val firstEvent = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0), 36)
        val secondEvent = eventService.createEvent("another title", LocalDateTime.now(), "city", Coord(0.0), 36)

        val team = TeamBuilder.createWith(teamService, userService, userDetailsService) {
            name = "Geiles Team"
            description = "Wir sind die geilsten"
            this.event = firstEvent
            creator {
                firstname = "Florian"
                lastname = "Schmidt"
                email = "florian@example.com"
                password = ""
            }
            invited {
                firstname = "Max"
                lastname = "Mustermann"
                email = "max@example.com"
                password = "pw"
            }
        }.build()

        val secondTeam = TeamBuilder.createWith(teamService, userService, userDetailsService) {
            name = "Noch ein geiles Team"
            description = "Wir sind auch nicht schlecht"
            this.event = secondEvent
            creator {
                firstname = "Fritz"
                lastname = "Fleißig"
                email = "fritz@example.com"
                password = "pw"
            }
            invited {
                firstname = "Hans"
                lastname = "Hurtig"
                email = "hurtig@example.com"
                password = "pw"
            }
        }.build()

        setAuthenticatedUser(team.members.first().email)
        challengeService.proposeChallenge(UnregisteredSponsor(
                firstname = "firstname",
                lastname = "lastname",
                company = "",
                url = "",
                gender = "",
                address = Address("", "", "", "", ""),
                email = "spon@example.com"
        ), team, euroOf(30.0), "description")

        challengeService.proposeChallenge(UnregisteredSponsor(
                firstname = "firstname",
                lastname = "lastname",
                company = "",
                url = "",
                gender = "",
                address = Address("", "", "", "", ""),
                email = "spon@example.com"
        ), team, euroOf(30.0), "description")

        setAuthenticatedUser(secondTeam.members.first().email)
        challengeService.proposeChallenge(UnregisteredSponsor(
                firstname = "firstname",
                lastname = "lastname",
                company = "",
                url = "",
                gender = "",
                address = Address("", "", "", "", ""),
                email = "spon@example.com"
        ), secondTeam, euroOf(30.0), "description")

        val foundAtFirst = this.challengeRepository.findAllUnregisteredSponsorsWithChallengesAtEvent(firstEvent.id!!)
        assertEquals(2, foundAtFirst.count())

        val foundAtSecond = this.challengeRepository.findAllUnregisteredSponsorsWithChallengesAtEvent(secondEvent.id!!)
        assertEquals(1, foundAtSecond.count())
    }

    @Test
    fun testFindAllUnregisteredSponsorsWhenOneSponsorSponsorsMultipleTeamsAtTheSameEvent() {

        val firstEvent = eventService.createEvent("title", LocalDateTime.now(), "city", Coord(0.0), 36)

        val team = TeamBuilder.createWith(teamService, userService, userDetailsService) {
            name = "Geiles Team"
            description = "Wir sind die geilsten"
            this.event = firstEvent
            creator {
                firstname = "Florian"
                lastname = "Schmidt"
                email = "florian@example.com"
                password = ""
            }
            invited {
                firstname = "Max"
                lastname = "Mustermann"
                email = "max@example.com"
                password = "pw"
            }
        }.build()

        val secondTeam = TeamBuilder.createWith(teamService, userService, userDetailsService) {
            name = "Noch ein geiles Team"
            description = "Wir sind auch nicht schlecht"
            this.event = firstEvent
            creator {
                firstname = "Fritz"
                lastname = "Fleißig"
                email = "fritz@example.com"
                password = "pw"
            }
            invited {
                firstname = "Hans"
                lastname = "Hurtig"
                email = "hurtig@example.com"
                password = "pw"
            }
        }.build()

        setAuthenticatedUser(team.members.first().email)

        challengeService.proposeChallenge(UnregisteredSponsor(
                firstname = "firstname",
                lastname = "lastname",
                company = "",
                url = "",
                gender = "",
                address = Address("", "", "", "", ""),
                email = "spon@example.com"
        ), team, euroOf(30.0), "description")

        challengeService.proposeChallenge(UnregisteredSponsor(
                firstname = "firstname",
                lastname = "lastname",
                company = "",
                url = "",
                gender = "",
                address = Address("", "", "", "", ""),
                email = "spon@example.com"
        ), team, euroOf(30.0), "description")

        val foundAtFirst = this.challengeRepository.findAllUnregisteredSponsorsWithChallengesAtEvent(firstEvent.id!!)

        assertEquals(2, foundAtFirst.count())
    }
}

class ParticipantBuilder(private val userService: UserService, init: ParticipantBuilder.() -> Unit) {

    init {
        init()
    }

    lateinit var firstname: String
    lateinit var lastname: String
    lateinit var email: String
    lateinit var password: String

    fun build(): Participant {
        return userService.create(email, password, {
            addRole(Participant::class).apply {
                firstname = firstname
                lastname = lastname
            }
        }).getRole(Participant::class)!!
    }
}

class TeamBuilder(private val teamService: TeamService,
                  private val userService: UserService,
                  private val userDetailsService: UserDetailsService,
                  init: TeamBuilder.() -> Unit) {

    fun setAuthenticatedUser(email: String) {
        val details = userDetailsService.loadUserByUsername(email)!! // Not null because otherwise exception is thrown
        val token = UsernamePasswordAuthenticationToken(details.username, details.password, details.authorities)
        SecurityContextHolder.getContext().authentication = token
    }

    init {
        init()
    }

    lateinit var name: String
    lateinit var description: String
    lateinit var creator: Participant
    lateinit var invited: Participant
    lateinit var event: Event

    fun creator(init: ParticipantBuilder.() -> Unit) {
        creator = ParticipantBuilder(userService, init).build()
    }

    fun invited(init: ParticipantBuilder.() -> Unit) {
        invited = ParticipantBuilder(userService, init).build()
    }

    fun build(): Team {
        val team = teamService.create(creator, name, description, event, null)

        setAuthenticatedUser(creator.email)
        teamService.invite(EmailAddress(invited.email), team)

        setAuthenticatedUser(invited.email)
        teamService.join(invited, team)

        return team
    }

    companion object {
        fun createWith(teamService: TeamService,
                       userService: UserService,
                       userDetailsService: UserDetailsService,
                       init: TeamBuilder.() -> Unit) = TeamBuilder(teamService, userService, userDetailsService, init)
    }

}
