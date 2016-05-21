package backend.configuration

import backend.model.event.EventService
import backend.model.event.TeamService
import backend.model.location.LocationService
import backend.model.misc.Coord
import backend.model.misc.EmailAddress
import backend.model.sponsoring.SponsoringService
import backend.model.user.Admin
import backend.model.user.Participant
import backend.model.user.Sponsor
import backend.model.user.UserService
import backend.util.Profiles.HEROKU
import org.javamoney.moneta.Money
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import javax.annotation.PostConstruct

@Service
@Profile(HEROKU)
class HerokuDataInitializer {

    // Services
    @Autowired lateinit private var userService: UserService
    @Autowired lateinit private var teamService: TeamService
    @Autowired lateinit private var eventService: EventService
    @Autowired lateinit private var locationService: LocationService
    @Autowired lateinit private var sponsoringService: SponsoringService
    @Autowired lateinit private var userDetailsService: UserDetailsService

    @PostConstruct
    fun initialize() {

        val date = LocalDateTime.of(2016, 6, 3, 0, 0)

        // ---- Events ----
        val eventMunich = eventService.createEvent("Breakout München 2016", date, "München", Coord(48.1374300, 11.5754900), 36)
        val eventBerlin = eventService.createEvent("Breakout Berlin 2016", date, "Berlin", Coord(52.5243700, 13.4105300), 36)

        // ---- Team 1 ----
        val participant1 = userService.create("participant1@break-out.org", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val participant2 = userService.create("participant2@break-out.org", "password", { addRole(Participant::class) }).getRole(Participant::class)!!

        val team1 = teamService.create(participant1, "Erstes Team", "Geile Sache", eventMunich)
        setAuthenticatedUser(participant1.email)
        teamService.invite(EmailAddress(participant2.email), team1)
        teamService.join(participant2, team1)

        // ---- Team 2 ----
        val participant3 = userService.create("participant3@break-out.org", "password", { addRole(Participant::class) }).getRole(Participant::class)!!
        val participant4 = userService.create("participant4@break-out.org", "password", { addRole(Participant::class) }).getRole(Participant::class)!!

        val team2 = teamService.create(participant3, "Zweites Team", "Immer noch geil", eventBerlin)
        setAuthenticatedUser(participant3.email)
        teamService.invite(EmailAddress(participant4.email), team2)
        teamService.join(participant4, team2)

        // ---- Admin ----
        userService.create("admin@break-out.org", "password", { addRole(Admin::class) })

        // ---- Sponsor1 ----
        val sponsor1 = userService.create("sponsor1@break-out.org", "password", { addRole(Sponsor::class) }).getRole(Sponsor::class)!!
        sponsoringService.createSponsoring(sponsor1, team1, Money.parse("EUR 0.1"), Money.parse("EUR 100"))

        // ---- Sponsor2 ----
        val sponsor2 = userService.create("sponsor2@break-out.org", "password", { addRole(Sponsor::class) }).getRole(Sponsor::class)!!
        sponsoringService.createSponsoring(sponsor2, team2, Money.parse("EUR 0.2"), Money.parse("EUR 200"))

        // ---- Locations for team1 ----
        locationService.create(Coord(51.0505, 13.7372), participant1, date.plusHours(1))
        locationService.create(Coord(48.8534100, 2.3488000), participant1, date.plusHours(2))

        // ---- Locations for team2 ----
        locationService.create(Coord(53.5753200, 10.0153400), participant3, date.plusHours(2))
        locationService.create(Coord(52.3740300, 4.8896900), participant3, date.plusHours(2))

    }

    private fun setAuthenticatedUser(email: String) {
        val details = userDetailsService.loadUserByUsername(email)!! // Not null because otherwise exception is thrown
        val token = UsernamePasswordAuthenticationToken(details.username, details.password, details.authorities)
        SecurityContextHolder.getContext().authentication = token
    }
}
