package backend.model.challenges

import backend.Integration.IntegrationTest
import backend.model.event.Event
import backend.model.event.Team
import backend.model.misc.Coord
import backend.model.sponsoring.TeamBuilder
import backend.model.sponsoring.UnregisteredSponsor
import backend.model.user.Address
import backend.model.user.Sponsor
import backend.util.euroOf
import org.junit.Before
import org.junit.Test
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.test.assertEquals

@Transactional()
class ChallengeRepositoryTest : IntegrationTest() {

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
    fun testFindAllRegisteredSponsorsWithSponsoringsAtEvent() {

        setAuthenticatedUser(firstRegisteredSponsor.email)
        sponsoringService.createSponsoring(firstRegisteredSponsor, firstTeamAtFirstEvent, euroOf(1.0), euroOf(10.0))
        sponsoringService.createSponsoring(firstRegisteredSponsor, firstTeamAtSecondEvent, euroOf(1.0), euroOf(10.0))


        setAuthenticatedUser(secondRegisteredSponsor.email)
        sponsoringService.createSponsoring(secondRegisteredSponsor, secondTeamAtSecondEvent, euroOf(1.0), euroOf(10.0))

        val fromFirst = sponsoringRepository.findAllRegisteredSponsorsWithSponsoringsAtEvent(firstEvent.id!!)
        val fromSecond = sponsoringRepository.findAllRegisteredSponsorsWithSponsoringsAtEvent(secondEvent.id!!)

        assertEquals(1, fromFirst.count())
        assertEquals(2, fromSecond.count())
    }

    @Test
    fun testFindAllRegisteredSponsorsWhenOneSponsorSponsorsMultipleTeamsAtTheSameEvent() {
        setAuthenticatedUser(firstRegisteredSponsor.email)

        sponsoringService.createSponsoring(firstRegisteredSponsor, firstTeamAtFirstEvent, euroOf(0.0), euroOf(10.0))
        sponsoringService.createSponsoring(firstRegisteredSponsor, secondTeamAtFirstEvent, euroOf(0.0), euroOf(10.0))

        val fromFirst = sponsoringRepository.findAllRegisteredSponsorsWithSponsoringsAtEvent(firstEvent.id!!)

        assertEquals(2, fromFirst.count())
    }

    @Test
    fun testFindAllRegisteredSponsorsWhenOneSponsorSponsorsMultipleTeamsAtDifferentEvents() {
        setAuthenticatedUser(firstRegisteredSponsor.email)

        sponsoringService.createSponsoring(firstRegisteredSponsor, firstTeamAtFirstEvent, euroOf(0.0), euroOf(1.0))
        sponsoringService.createSponsoring(firstRegisteredSponsor, firstTeamAtSecondEvent, euroOf(0.0), euroOf(1.0))

        val fromFirst = sponsoringRepository.findAllRegisteredSponsorsWithSponsoringsAtEvent(firstEvent.id!!)
        val fromSecond = sponsoringRepository.findAllRegisteredSponsorsWithSponsoringsAtEvent(secondEvent.id!!)

        assertEquals(1, fromFirst.count())
        assertEquals(1, fromSecond.count())
    }

    @Test
    fun testFindAllUnregisteredSponsors() {
        val firstUnregistered = UnregisteredSponsor(
                firstname = "Firstname",
                lastname = "",
                company = "",
                url = "",
                gender = "",
                address = Address("", "", "", "", ""),
                email = "spon@example.com"
        )

        val secondUnregistered = UnregisteredSponsor(
                firstname = "Firstname",
                lastname = "",
                company = "",
                url = "",
                gender = "",
                address = Address("", "", "", "", ""),
                email = "spon@example.com"
        )

        val thirdUnregistered = UnregisteredSponsor(
                firstname = "Firstname",
                lastname = "",
                company = "",
                url = "",
                gender = "",
                address = Address("", "", "", "", ""),
                email = "spon@example.com"
        )

        setAuthenticatedUser(firstTeamAtFirstEvent.members.first().email)
        sponsoringService.createSponsoringWithOfflineSponsor(firstTeamAtFirstEvent, euroOf(10), euroOf(10), firstUnregistered)

        setAuthenticatedUser(secondTeamAtFirstEvent.members.first().email)
        sponsoringService.createSponsoringWithOfflineSponsor(secondTeamAtFirstEvent, euroOf(10), euroOf(10), secondUnregistered)

    }
}
