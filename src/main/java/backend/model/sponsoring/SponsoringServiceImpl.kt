package backend.model.sponsoring

import backend.model.event.Team
import backend.model.event.TeamService
import backend.model.misc.Email
import backend.model.misc.EmailAddress
import backend.model.user.Sponsor
import backend.services.MailService
import org.javamoney.moneta.Money
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal
import javax.transaction.Transactional

@Service
class SponsoringServiceImpl : SponsoringService {

    private val sponsoringRepository: SponsoringRepository
    private val mailService: MailService
    private val teamService: TeamService


    @Autowired
    constructor(sponsoringRepository: SponsoringRepository, mailService: MailService, teamService: TeamService) {
        this.sponsoringRepository = sponsoringRepository
        this.mailService = mailService
        this.teamService = teamService
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
