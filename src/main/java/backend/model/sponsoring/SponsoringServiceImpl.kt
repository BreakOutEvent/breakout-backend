package backend.model.sponsoring

import backend.model.event.Team
import backend.model.event.TeamService
import backend.model.misc.Email
import backend.model.misc.EmailAddress
import backend.model.user.Sponsor
import backend.model.user.UserService
import backend.services.MailService
import org.javamoney.moneta.Money
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal
import javax.transaction.Transactional

@Service
class SponsoringServiceImpl : SponsoringService {

    private val sponsoringRepository: SponsoringRepository
    private val mailService: MailService
    private val teamService: TeamService
    private val userService: UserService
    private val logger: Logger


    @Autowired
    constructor(sponsoringRepository: SponsoringRepository,
                mailService: MailService,
                teamService: TeamService,
                userService: UserService) {

        this.sponsoringRepository = sponsoringRepository
        this.mailService = mailService
        this.teamService = teamService
        this.userService = userService
        this.logger = LoggerFactory.getLogger(SponsoringServiceImpl::class.java)
    }

    @Transactional
    override fun createSponsoring(sponsor: Sponsor, team: Team, amountPerKm: Money, limit: Money): Sponsoring {
        val sponsoring = Sponsoring(sponsor, team, amountPerKm, limit)


        val email = Email(
                to = team.members.map { EmailAddress(it.email) },
                subject = "BreakOut 2016 - Euch wurde ein Sponsoring hinzugefügt!",
                body = "Hallo Team \"${team.name}\" Euch wurde ein Sponsoring hinzugefügt!<br><br>" +
                        "Je Kilometer den Ihr zurücklegt erhaltet Ihr ${amountPerKm.numberStripped.toPlainString()}€ an " +
                        "zusätzlichen Sponsorengeldern, mit einem Limit von maximal ${limit.numberStripped.toPlainString()}€.<br>" +
                        "Du hast Fragen oder benötigst Unterstützung? Schreib uns eine E-Mail an <a href=\"event@break-out.org\">event@break-out.org</a>.<br><br>" +
                        "Liebe Grüße<br>" +
                        "Euer BreakOut-Team",
                campaignCode = "sponsoring_added"
        )

        mailService.send(email)

        return sponsoringRepository.save(sponsoring)
    }

    override fun sendEmailsToSponsorsWhenEventHasStarted() {

        userService.findAllSponsors()
                .filter { it.challenges.count() + it.sponsorings.count() > 0 }
                .apply { logger.info("Sending emails that event has started to ${this.count()} sponsors") }
                .forEach {
                    val mail = Email(
                            to = listOf(EmailAddress(it.email)),
                            subject = "BreakOut 2016 - Jetzt geht's los",
                            body = getEmailBodyWhenEventHasStarted(it),
                            buttonText = "ZUM LIVEBLOG",
                            buttonUrl = "https://event.break-out.org/?utm_source=backend&utm_medium=email&utm_content=intial&utm_campaign=event_started_sponsor")

                    mailService.sendAsync(mail)
                }
    }

    private fun getEmailBodyWhenEventHasStarted(sponsor: Sponsor): String {
        val title = when (sponsor.gender) {
            "male" -> "Sehr geehrter Herr"
            "female" -> "Sehr geehrte Frau"
            else -> "Sehr geehrte Frau / Herr"
        }

        return "$title ${sponsor.firstname} ${sponsor.lastname},<br><br>" +
                "BreakOut 2016 hat begonnen! Wir freuen uns sehr, Sie als Sponsor dabei zu haben!<br>" +
                "Sie können unter <a href=\"https://event.break-out.org/?utm_source=backend&utm_medium=email&u" +
                "tm_content=intial&utm_campaign=event_started_sponsor\">https://event.break-out.org/</a> die nächsten 36 Stunden live mitverfolgen, " +
                "wohin die Reise geht und welche Abenteuer Ihr Team dabei erlebt. Natürlich können Sie " +
                "während der 36h Ihr Team noch mit spontanen Challenges herausfordern. " +
                "Wir wünschen Ihnen viel Spaß dabei!!<br><br>" +
                "Herzliche Grüße<br>" +
                "Ihr BreakOut-Team"
    }

    @Transactional
    override fun createSponsoringWithOfflineSponsor(team: Team,
                                                    amountPerKm: Money,
                                                    limit: Money,
                                                    unregisteredSponsor: UnregisteredSponsor): Sponsoring {
        val sponsoring = Sponsoring(unregisteredSponsor, team, amountPerKm, limit)
        return sponsoringRepository.save(sponsoring)
    }

    @Transactional
    override fun acceptSponsoring(sponsoring: Sponsoring): Sponsoring {
        sponsoring.accept()
        return sponsoringRepository.save(sponsoring)
    }

    @Transactional
    override fun rejectSponsoring(sponsoring: Sponsoring): Sponsoring {
        sponsoring.reject()
        return sponsoringRepository.save(sponsoring)
    }

    @Transactional
    override fun withdrawSponsoring(sponsoring: Sponsoring): Sponsoring {
        sponsoring.withdraw()

        if (sponsoring.hasRegisteredSponsor()) {
            val email = Email(
                    to = sponsoring.team!!.members.map { EmailAddress(it.email) },
                    subject = "BreakOut 2016 - Ein Sponsoring wurde zurückgezogen!",
                    body = "Hallo Team \"${sponsoring.team!!.name}\" ein Sponsoring wurde zurückgezogen, vielleicht wollt ihr mit dem Sponsor noch einmal darüber reden.<br>" +
                            "Du hast Fragen oder benötigst Unterstützung? Schreib uns eine E-Mail an <a href=\"event@break-out.org\">event@break-out.org</a>.<br><br>" +
                            "Liebe Grüße<br>" +
                            "Euer BreakOut-Team",
                    campaignCode = "sponsoring_withdrawn"
            )

            mailService.send(email)
        }

        return sponsoringRepository.save(sponsoring)
    }

    override fun findByTeamId(teamId: Long) = sponsoringRepository.findByTeamId(teamId)

    override fun findBySponsorId(sponsorId: Long) = sponsoringRepository.findBySponsorCoreId(sponsorId)

    override fun findOne(id: Long): Sponsoring? = sponsoringRepository.findOne(id)

    fun getAmountRaised(sponsoring: Sponsoring): Money {
        if (reachedLimit(sponsoring)) {
            return sponsoring.limit
        } else {
            return calculateAmount(sponsoring)
        }
    }

    fun reachedLimit(sponsoring: Sponsoring): Boolean {
        return calculateAmount(sponsoring).isGreaterThan(sponsoring.limit)
    }

    fun calculateAmount(sponsoring: Sponsoring): Money {
        val kilometers = teamService.getLinearDistanceForTeam(sponsoring.team!!.id!!)
        val amountPerKmAsBigDecimal = sponsoring.amountPerKm.numberStripped
        val total = amountPerKmAsBigDecimal.multiply(BigDecimal.valueOf(kilometers))

        return Money.of(total, "EUR")
    }
}
