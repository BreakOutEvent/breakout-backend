package backend.controller

import backend.controller.exceptions.NotFoundException
import backend.model.cache.CacheService
import backend.model.challenges.Challenge
import backend.model.challenges.ChallengeService
import backend.model.challenges.ChallengeStatus
import backend.model.event.EventService
import backend.model.event.TeamService
import backend.model.misc.Email
import backend.model.misc.EmailAddress
import backend.model.misc.EmailRepository
import backend.model.payment.SponsoringInvoiceService
import backend.model.sponsoring.Sponsoring
import backend.model.sponsoring.SponsoringService
import backend.model.sponsoring.SponsoringStatus
import backend.model.user.UserService
import backend.services.MailService
import org.javamoney.moneta.Money
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod.GET
import org.springframework.web.bind.annotation.RequestMethod.POST
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.math.RoundingMode

@RestController
@RequestMapping("/admin")
open class AdminController {


    private val mailService: MailService
    private val teamService: TeamService
    private val sponsoringService: SponsoringService
    private val challengeService: ChallengeService
    private val userService: UserService
    private val emailRepository: EmailRepository
    private val sponsoringInvoiceService: SponsoringInvoiceService
    private val eventService: EventService
    private val cacheService: CacheService
    private val logger: Logger

    @Autowired
    constructor(mailService: MailService,
                teamService: TeamService,
                sponsoringService: SponsoringService,
                challengeService: ChallengeService,
                userService: UserService,
                emailRepository: EmailRepository,
                eventService: EventService,
                cacheService: CacheService,
                sponsoringInvoiceService: SponsoringInvoiceService) {

        this.mailService = mailService
        this.teamService = teamService
        this.userService = userService
        this.sponsoringService = sponsoringService
        this.challengeService = challengeService
        this.emailRepository = emailRepository
        this.sponsoringInvoiceService = sponsoringInvoiceService
        this.eventService = eventService
        this.cacheService = cacheService
        this.logger = LoggerFactory.getLogger(AdminController::class.java)
    }


    /**
     * GET /admin/regeneratecache/
     * Allows Admin to resend failed mails
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping("/regeneratecache/", method = arrayOf(GET))
    open fun regenerateCache(): String {
        logger.info("Regenerating caches from admin request")

        eventService.regenerateCache()
        return "done"
    }

    /**
     * GET /admin/resendmail/
     * Allows Admin to resend failed mails
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping("/resendmail/", method = arrayOf(GET))
    open fun resendMail(): Map<String, Int> {
        val count = mailService.resendFailed()

        logger.info("Resent $count mails from admin request")
        return mapOf("count" to count)
    }

    /**
     * POST /admin/email/{identifier}/send/
     * Send emails for specific identifiers
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping("/email/{identifier}/send/", method = arrayOf(POST))
    open fun sendEmail(@PathVariable identifier: String): Map<String, String> {
        when (identifier) {
            "SPONSOR_EVENT_STARTED" -> sponsoringService.sendEmailsToSponsorsWhenEventHasStarted()
            "SPONSOR_EVENT_ENDED" -> sponsoringService.sendEmailsToSponsorsWhenEventHasEnded()
            "TEAM_EVENT_ENDED" -> teamService.sendEmailsToTeamsWhenEventHasEnded()
            else -> throw NotFoundException("identifier $identifier not registered as email trigger")
        }

        return mapOf("message" to "success")
    }

    /**
     * GET /admin/email/{identifier}/generate/
     * Send emails for specific identifiers
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping("/email/{identifier}/generate/", method = arrayOf(GET))
    open fun generateEmail(@PathVariable identifier: String,
                           @RequestParam(value = "save", required = false) save: String?,
                           @RequestParam(value = "invoices", required = false) invoices: String?): List<Map<String, Any>> {
        when (identifier) {
            "REGISTERED_SPONSORS_PAYMENT_PROMPT" -> {
                val data = getRegisteredSponsorsData()
                if (save != null && save == "true") generateRegisteredSponsorEmail(data)
                if (invoices != null && invoices == "true") generateRegisteredSponsorInvoices(data)
                return data
            }
            "UNREGISTERED_SPONSORS_PAYMENT_PROMPT" -> {
                val data = getUnregisteredSponsorsData()
                if (save != null && save == "true") generateUnregisteredSponsorEmail(data)
                if (invoices != null && invoices == "true") generateUnregisteredSponsorInvoices(data)
                return data
            }
            else -> throw NotFoundException("identifier $identifier not registered as email trigger")
        }
    }


    private fun generateUnregisteredSponsorInvoices(data: List<Map<String, Any>>) {

        data.forEach { team ->

            val sponsorSum = mutableMapOf<String, BigDecimal>()
            val sponsorSponsorings = mutableMapOf<String, MutableList<Sponsoring>>()
            val sponsorChallenges = mutableMapOf<String, MutableList<Challenge>>()


            (team["sponsorings"] as List<Map<String, Any>>).forEach { sponsoring ->
                val sponsorAddress = "${sponsoring["sponsor_street"]} ${sponsoring["sponsor_housenumber"]} ${sponsoring["sponsor_zipcode"]} ${sponsoring["sponsor_city"]}"
                sponsorSum.put(sponsorAddress, (sponsorSum.getOrElse(sponsorAddress) { BigDecimal.ZERO }).add(sponsoring["sponsor_amount_sum"] as BigDecimal))

                val thisSponsoring = sponsoringService.findOne(sponsoring["sponsoring_id"] as Long)!!
                val sponsoringList = (sponsorSponsorings.getOrElse(sponsorAddress) { mutableListOf() })
                sponsoringList.add(thisSponsoring)
                sponsorSponsorings.put(sponsorAddress, sponsoringList)

            }

            (team["challenges"] as List<Map<String, Any>>).forEach { challenge ->

                val sponsorAddress = "${challenge["sponsor_street"]} ${challenge["sponsor_housenumber"]} ${challenge["sponsor_zipcode"]} ${challenge["sponsor_city"]}"
                sponsorSum.put(sponsorAddress, (sponsorSum.getOrElse(sponsorAddress) { BigDecimal.ZERO }).add(challenge["challenge_amount"] as BigDecimal))

                val thisChallenge = challengeService.findOne(challenge["challenge_id"] as Long)!!
                val challengeList = (sponsorChallenges.getOrElse(sponsorAddress) { mutableListOf() })
                challengeList.add(thisChallenge)
                sponsorChallenges.put(sponsorAddress, challengeList)
            }

            sponsorSum.forEach { sponsor ->

                var subject = "BreakOut Team ${team["team_id"]} ${sponsor.key}"
                if (subject.length > 160) subject = subject.substring(0, 160)

                val invoiceTeam = teamService.findOne(team["team_id"] as Long)!!
                val amount = Money.of(sponsor.value, "EUR")

                sponsoringInvoiceService.createInvoice(invoiceTeam, amount, subject, sponsorSponsorings.getOrElse(sponsor.key) { mutableListOf() }, sponsorChallenges.getOrElse(sponsor.key) { mutableListOf() })
            }

        }

    }

    private fun generateRegisteredSponsorInvoices(data: List<Map<String, Any>>) {

        data.forEach { sponsor ->

            val teamSum = mutableMapOf<Long, BigDecimal>()
            val teamSponsorings = mutableMapOf<Long, MutableList<Sponsoring>>()
            val teamChallenges = mutableMapOf<Long, MutableList<Challenge>>()


            (sponsor["sponsorings"] as List<Map<String, Any>>).forEach { sponsoring ->

                val team_id = sponsoring["team_id"] as Long
                teamSum.put(team_id, (teamSum.getOrElse(team_id) { BigDecimal.ZERO }).add(sponsoring["sponsor_amount_sum"] as BigDecimal))

                val thisSponsoring = sponsoringService.findOne(sponsoring["sponsoring_id"] as Long)!!
                val sponsoringList = (teamSponsorings.getOrElse(team_id) { mutableListOf() })
                sponsoringList.add(thisSponsoring)
                teamSponsorings.put(team_id, sponsoringList)

            }

            (sponsor["challenges"] as List<Map<String, Any>>).forEach { challenge ->

                val team_id = challenge["team_id"] as Long
                teamSum.put(team_id, (teamSum.getOrElse(team_id) { BigDecimal.ZERO }).add(challenge["challenge_amount"] as BigDecimal))

                val thisChallenge = challengeService.findOne(challenge["challenge_id"] as Long)!!
                val challengeList = (teamChallenges.getOrElse(team_id) { mutableListOf() })
                challengeList.add(thisChallenge)
                teamChallenges.put(team_id, challengeList)

            }

            teamSum.forEach { team ->

                var subject = "BreakOut Team ${team.key} ${sponsor["sponsor_street"]} ${sponsor["sponsor_housenumber"]} ${sponsor["sponsor_zipcode"]} ${sponsor["sponsor_city"]}"
                if (subject.length > 160) subject = subject.substring(0, 160)

                val invoiceTeam = teamService.findOne(team.key)!!
                val amount = Money.of(team.value, "EUR")

                sponsoringInvoiceService.createInvoice(invoiceTeam, amount, subject, teamSponsorings.getOrElse(team.key) { mutableListOf() }, teamChallenges.getOrElse(team.key) { mutableListOf() })

            }

        }
    }

    private fun generateRegisteredSponsorEmail(data: List<Map<String, Any>>) {

        data.forEach { sponsor ->

            val teamSum = mutableMapOf<Long, BigDecimal>()

            var body = "Liebe Sponsoren,<br><br>" +
                    "vielen herzlichen Dank für Ihre Unterstützung! Insgesamt wurden von den BreakOut Teams 104.721 Kilometer zurückgelegt und unzählige Challenges gemeistert.<br>" +
                    "Doch ohne Ihre Hilfe als Sponsor wäre der soziale Gedanke hinter diesem Projekt nicht umzusetzen. Deswegen freuen wir uns, wenn Sie Ihr Spendenversprechen bis zum 10.6., erfüllen.<br>" +
                    "Hier eine Aufschlüsselung Ihres Spendenversprechens:<br><br>"

            (sponsor["sponsorings"] as List<Map<String, Any>>).forEach { sponsoring ->
                body += " - Sponsoring für Team \"${sponsoring["team_name"]}\" mit ${(sponsoring["team_distance"] as BigDecimal).setScale(2, RoundingMode.HALF_UP).toPlainString()}KM à " +
                        "${(sponsoring["sponsor_amount_per_km"] as BigDecimal).setScale(2, RoundingMode.HALF_UP).toPlainString()}€  = " +
                        "${(sponsoring["sponsor_amount_sum"] as BigDecimal).setScale(2, RoundingMode.HALF_UP).toPlainString()}€ ${if (sponsoring["is_limited"] as Boolean) "am gesetzten Limit" else ""}<br>"

                val team_id = sponsoring["team_id"] as Long
                teamSum.put(team_id, (teamSum.getOrElse(team_id) { BigDecimal.ZERO }).add(sponsoring["sponsor_amount_sum"] as BigDecimal))
            }

            if ((sponsor["sponsorings"] as List<*>).count() > 0 && (sponsor["challenges"] as List<*>).count() > 0) body += "<br>"

            (sponsor["challenges"] as List<Map<String, Any>>).forEach { challenge ->
                body += " - Challenge <i>\"${challenge["challenge_description"]}\"</i> an Team \"${challenge["team_name"]}\" erfüllt = " +
                        "${(challenge["challenge_amount"] as BigDecimal).setScale(2, RoundingMode.HALF_UP).toPlainString()}€<br>"

                val team_id = challenge["team_id"] as Long
                teamSum.put(team_id, (teamSum.getOrElse(team_id) { BigDecimal.ZERO }).add(challenge["challenge_amount"] as BigDecimal))
            }

            body += "<br>Insgesamt ergibt sich aus den eingetragenen Sponsorings und Challenges ein Spendenversprechen von ${(sponsor["sponsorings_sum"] as BigDecimal).add(sponsor["challenges_sum"] as BigDecimal).setScale(2, RoundingMode.HALF_UP).toPlainString()}€. " +
                    "Wenn Sie mehrere Teams unterstützt haben, sehen Sie dort mehrere Verwendungszwecke. Bitte tätigen Sie diese Überweisungen einzeln, damit wir die Spenden jedem Team korrekt zuordnen können.<br><br>"

            teamSum.forEach { team ->

                var subject = "BreakOut Team ${team.key} ${sponsor["sponsor_street"]} ${sponsor["sponsor_housenumber"]} ${sponsor["sponsor_zipcode"]} ${sponsor["sponsor_city"]}"
                if (subject.length > 160) subject = subject.substring(0, 160)

                body += "BETRAG: ${team.value}€<br>" +
                        "VERWENDUNGSZWECK: $subject<br><br>"
            }

            body += "KONTOINHABER: UNO-Flüchtlingshilfe<br>" +
                    "BANKNAME: Sparkasse KölnBonn<br>" +
                    "IBAN: DE78 3705 0198 0020 0088 50<br>" +
                    "BIC: COLSDE33XXX<br><br><br>"

            body += "Besonders wichtig ist der Verwendungszweck, denn nur so können Ihre Spenden dem richtigen Team zugeordnet werden. Darüber hinaus kann die UNO-Flüchtlingshilfe Ihnen nur bei Eingabe Ihrer Adresse eine offizielle Spendenquittung zusenden.<br><br>" +
                    "Bei Fragen können Sie sich gerne an Ihr Team oder auch an uns wenden.<br><br>" +
                    "Wir wünschen Ihnen eine schöne Woche<br>" +
                    "Euer BreakOut-Team"

            val email = Email(
                    to = listOf(EmailAddress(sponsor["sponsor_email"] as String)),
                    subject = "BreakOut 2016 - Ihr Spendenversprechen",
                    body = body,
                    campaignCode = "registrated_sponsor_payment"
            )

            email.isSent = false
            emailRepository.save(email)
        }
    }

    private fun generateUnregisteredSponsorEmail(data: List<Map<String, Any>>) {

        data.forEach { team ->

            val sponsorSum = mutableMapOf<String, BigDecimal>()
            val sponsorNames = mutableMapOf<String, String>()

            var body = "Liebe Teams,<br><br>" +
                    "Danke, dass Ihr dabei gewesen seid! Ganz ehrlich: Ihr seid GEIL!<br><br>" +
                    "In dieser E-Mail schicken wir euch eine Liste von Verwendungszwecken, die Ihr an die entsprechenden Sponsoren weiter reichen müsst, damit wir euch die Spenden anrechnen können! " +
                    "Bitte stellt sicher, dass die Überweisungen bis zum 10.6 bei uns ankommen, damit wir die finale Summe bei der Siegerehrung bekannt geben können!<br><br>" +
                    "Hier eine Aufschlüsselung eurer offline Spendenversprechen:<br><br>"

            (team["sponsorings"] as List<Map<String, Any>>).forEach { sponsoring ->
                body += " - Sponsoring durch \"${sponsoring["sponsor_firstname"]} ${sponsoring["sponsor_lastname"]}\" mit ${(sponsoring["team_distance"] as BigDecimal).setScale(2, RoundingMode.HALF_UP).toPlainString()}KM à " +
                        "${(sponsoring["sponsor_amount_per_km"] as BigDecimal).setScale(2, RoundingMode.HALF_UP).toPlainString()}€  = " +
                        "${(sponsoring["sponsor_amount_sum"] as BigDecimal).setScale(2, RoundingMode.HALF_UP).toPlainString()}€ ${if (sponsoring["is_limited"] as Boolean) "am gesetzten Limit" else ""}<br>"

                val sponsorAddress = "${sponsoring["sponsor_street"]} ${sponsoring["sponsor_housenumber"]} ${sponsoring["sponsor_zipcode"]} ${sponsoring["sponsor_city"]}"
                sponsorSum.put(sponsorAddress, (sponsorSum.getOrElse(sponsorAddress) { BigDecimal.ZERO }).add(sponsoring["sponsor_amount_sum"] as BigDecimal))
                sponsorNames.put(sponsorAddress, "${sponsoring["sponsor_firstname"]} ${sponsoring["sponsor_lastname"]}")
            }

            if ((team["sponsorings"] as List<*>).count() > 0 && (team["challenges"] as List<*>).count() > 0) body += "<br>"

            (team["challenges"] as List<Map<String, Any>>).forEach { challenge ->
                body += " - Challenge <i>\"${challenge["challenge_description"]}\"</i> gestellt von \"${challenge["sponsor_firstname"]} ${challenge["sponsor_lastname"]}\" erfüllt = " +
                        "${(challenge["challenge_amount"] as BigDecimal).setScale(2, RoundingMode.HALF_UP).toPlainString()}€<br>"

                val sponsorAddress = "${challenge["sponsor_street"]} ${challenge["sponsor_housenumber"]} ${challenge["sponsor_zipcode"]} ${challenge["sponsor_city"]}"
                sponsorSum.put(sponsorAddress, (sponsorSum.getOrElse(sponsorAddress) { BigDecimal.ZERO }).add(challenge["challenge_amount"] as BigDecimal))
                sponsorNames.put(sponsorAddress, "${challenge["sponsor_firstname"]} ${challenge["sponsor_lastname"]}")
            }

            body += "<br>Falls in dieser Liste Challenges oder Sponsorings fehlen, die ihr nicht eingetragen oder angenommen habt, dann sprecht das bitte direkt mit den entsprechenden Sponsoren ab. Diese können ein dann ebenfalls eine Überweisung nach dem unten erkennbaren Muster BreakOut Team [Nr] [SPONSOR ADRESSE] tätigen. Das zählen wir dann auch in die Summe für euer Team.<br><br>"

            body += "Insgesamt ergibt sich aus den eingetragenen Sponsorings und Challenges ein Spendenversprechen von ${(team["sponsorings_sum"] as BigDecimal).add(team["challenges_sum"] as BigDecimal).setScale(2, RoundingMode.HALF_UP).toPlainString()}€. " +
                    "Wenn Sie mehrere Teams unterstützt haben, sehen Sie dort mehrere Verwendungszwecke. Bitte tätigen Sie diese Überweisungen einzeln, damit wir die Spenden jedem Team korrekt zuordnen können.<br><br>"

            sponsorSum.forEach { sponsor ->

                var subject = "BreakOut Team ${team["team_id"]} ${sponsor.key}"
                if (subject.length > 160) subject = subject.substring(0, 160)

                body += "SPONSOR: ${sponsorNames[sponsor.key]}<br>"
                body += "BETRAG: ${sponsor.value}€<br>" +
                        "VERWENDUNGSZWECK: $subject<br><br>"
            }

            body += "KONTOINHABER: UNO-Flüchtlingshilfe<br>" +
                    "BANKNAME: Sparkasse KölnBonn<br>" +
                    "IBAN: DE78 3705 0198 0020 0088 50<br>" +
                    "BIC: COLSDE33XXX<br><br><br>"

            body += "Wenn die Sponsoren die Adresse nicht angeben wollen ist das auch okay, dann bekommen Sie allerdings keine Spendenquittung. Wenn der Verwendungszweck genau so angegeben wird wie oben geschrieben, erhalten die Sponsoren nach einiger Zeit automatisch eine Spendenquittung per Post.<br><br>" +
                    "Bei Fragen können Sie sich gerne an Ihr Team oder auch an uns wenden.<br><br>" +
                    "Ihr seid die Besten!<br><br>" +

                    "Liebe Grüße,<br>" +
                    "Euer BreakOut-Team"

            val email = Email(
                    to = (team["team_emails"] as List<String>).map(::EmailAddress),
                    subject = "BreakOut 2016 - Ihr Spendenversprechen",
                    body = body,
                    campaignCode = "unregistrated_sponsor_payment"
            )

            email.isSent = false
            emailRepository.save(email)
        }

    }

    private fun getUnregisteredSponsorsData(): List<Map<String, Any>> {
        var unregisteredSum = BigDecimal.ZERO
        var unregisteredSponsorSum = BigDecimal.ZERO
        var unregisteredChallengeSum = BigDecimal.ZERO
        val unregisterdSponsorsData = mutableListOf<Map<String, Any>>()

        // Each Team
        val teams = teamService.findAll()

        teams.forEach { team ->

            val sponsorings = sponsoringService.findByTeamId(team.id!!).filter { !it.hasRegisteredSponsor() && (it.status == SponsoringStatus.ACCEPTED || it.status == SponsoringStatus.PAYED) }
            val challenges = challengeService.findByTeamId(team.id!!).filter { !it.hasRegisteredSponsor() && (it.status == ChallengeStatus.WITH_PROOF || it.status == ChallengeStatus.PROOF_ACCEPTED) }

            if (sponsorings.count() > 0 || challenges.count() > 0) {
                val sponsorMap = mutableMapOf<String, Any>()
                val sponsoringsMap = mutableListOf<Map<String, Any>>()
                val challengesMap = mutableListOf<Map<String, Any>>()

                var sponsoringAmount = BigDecimal.ZERO
                var challengeAmount = BigDecimal.ZERO


                // Sponsorings
                sponsorings.forEach { sponsoring ->

                    var sponsorSum = BigDecimal.ZERO
                    var isLimit = false
                    val distanceKm = BigDecimal.valueOf(teamService.getLinearDistanceForTeam(sponsoring.team!!.id!!)).setScale(2, BigDecimal.ROUND_HALF_UP)

                    val amount = sponsoring.amountPerKm.numberStripped.multiply(distanceKm)
                    if (amount.compareTo(sponsoring.limit.numberStripped) == 1) {
                        sponsorSum = sponsorSum.add(sponsoring.limit.numberStripped)
                        isLimit = true
                    } else {
                        sponsorSum = sponsorSum.add(amount)
                    }

                    sponsorSum = sponsorSum.setScale(2, BigDecimal.ROUND_HALF_UP)
                    sponsoringAmount = sponsoringAmount.add(sponsorSum)

                    sponsoringsMap.add(mapOf(
                            "team_id" to sponsoring.team!!.id!!,
                            "team_name" to sponsoring.team!!.name,
                            "team_distance" to distanceKm,
                            "sponsor_amount_per_km" to sponsoring.amountPerKm.numberStripped,
                            "is_limited" to isLimit,
                            "sponsoring_id" to sponsoring.id!!,
                            "sponsor_amount_sum" to sponsorSum,
                            "sponsor_firstname" to (sponsoring.sponsor.firstname ?: ""),
                            "sponsor_lastname" to (sponsoring.sponsor.lastname ?: ""),
                            "sponsor_street" to (sponsoring.sponsor.address.street),
                            "sponsor_housenumber" to (sponsoring.sponsor.address.housenumber),
                            "sponsor_zipcode" to (sponsoring.sponsor.address.zipcode),
                            "sponsor_city" to (sponsoring.sponsor.address.city)
                    ))
                }

                // Challenges
                challenges.forEach { challenge ->
                    challengeAmount = challengeAmount.add(challenge.amount.numberStripped)

                    challengesMap.add(mapOf(
                            "team_id" to challenge.team!!.id!!,
                            "team_name" to challenge.team!!.name,
                            "challenge_description" to challenge.description,
                            "challenge_amount" to challenge.amount.numberStripped,
                            "challenge_id" to challenge.id!!,
                            "sponsor_firstname" to (challenge.sponsor.firstname ?: ""),
                            "sponsor_lastname" to (challenge.sponsor.lastname ?: ""),
                            "sponsor_street" to challenge.sponsor.address.street,
                            "sponsor_housenumber" to challenge.sponsor.address.housenumber,
                            "sponsor_zipcode" to challenge.sponsor.address.zipcode,
                            "sponsor_city" to challenge.sponsor.address.city
                    ))
                }

                // Summen Ausgabe
                sponsorMap.put("team_id", team.id!!)
                sponsorMap.put("team_emails", team.members.map { it.email })
                sponsorMap.put("sponsorings", sponsoringsMap)
                sponsorMap.put("challenges", challengesMap)
                sponsorMap.put("sponsorings_sum", sponsoringAmount)
                sponsorMap.put("challenges_sum", challengeAmount)

                unregisteredSum = unregisteredSum.add(sponsoringAmount).add(challengeAmount)
                unregisteredChallengeSum = unregisteredChallengeSum.add(challengeAmount)
                unregisteredSponsorSum = unregisteredSponsorSum.add(sponsoringAmount)

                unregisterdSponsorsData.add(sponsorMap)
            }

            print(".")
        }

        println()
        println("unregisteredSum: ${unregisteredSum.toPlainString()}")
        println("unregisteredChallengeSum: ${unregisteredChallengeSum.toPlainString()}")
        println("unregisteredSponsorSum: ${unregisteredSponsorSum.toPlainString()}")

        return unregisterdSponsorsData
    }

    private fun getRegisteredSponsorsData(): List<Map<String, Any>> {
        var registeredSum = BigDecimal.ZERO
        var registeredChallengeSum = BigDecimal.ZERO
        var registeredSponsorSum = BigDecimal.ZERO
        val registerdSponsorsData = mutableListOf<Map<String, Any>>()

        // Each Registered Sponsor
        val registeredSponsors = userService.findAllSponsors()

        registeredSponsors.forEach { sponsor ->

            val sponsorings = sponsoringService.findBySponsorId(sponsor.account.id!!).filter { it.status == SponsoringStatus.ACCEPTED || it.status == SponsoringStatus.PAYED }
            val challenges = challengeService.findBySponsorId(sponsor.account.id!!).filter { it.status == ChallengeStatus.WITH_PROOF || it.status == ChallengeStatus.PROOF_ACCEPTED }

            if (sponsorings.count() > 0 || challenges.count() > 0) {
                val sponsorMap = mutableMapOf<String, Any>()
                val sponsoringsMap = mutableListOf<Map<String, Any>>()
                val challengesMap = mutableListOf<Map<String, Any>>()

                var sponsoringAmount = BigDecimal.ZERO
                var challengeAmount = BigDecimal.ZERO


                // Sponsorings
                sponsorings.forEach { sponsoring ->

                    var sponsorSum = BigDecimal.ZERO
                    var isLimit = false
                    val distanceKm = BigDecimal.valueOf(teamService.getLinearDistanceForTeam(sponsoring.team!!.id!!)).setScale(2, BigDecimal.ROUND_HALF_UP)

                    val amount = sponsoring.amountPerKm.numberStripped.multiply(distanceKm)
                    if (amount.compareTo(sponsoring.limit.numberStripped) == 1) {
                        sponsorSum = sponsorSum.add(sponsoring.limit.numberStripped)
                        isLimit = true
                    } else {
                        sponsorSum = sponsorSum.add(amount)
                    }

                    sponsorSum = sponsorSum.setScale(2, BigDecimal.ROUND_HALF_UP)
                    sponsoringAmount = sponsoringAmount.add(sponsorSum)

                    sponsoringsMap.add(mapOf(
                            "team_id" to sponsoring.team!!.id!!,
                            "sponsoring_id" to sponsoring.id!!,
                            "team_name" to sponsoring.team!!.name,
                            "team_distance" to distanceKm,
                            "sponsor_amount_per_km" to sponsoring.amountPerKm.numberStripped,
                            "is_limited" to isLimit,
                            "sponsor_amount_sum" to sponsorSum
                    ))
                }

                // Challenges
                challenges.forEach { challenge ->
                    challengeAmount = challengeAmount.add(challenge.amount.numberStripped)

                    challengesMap.add(mapOf(
                            "team_id" to challenge.team!!.id!!,
                            "challenge_id" to challenge.id!!,
                            "team_name" to challenge.team!!.name,
                            "challenge_description" to challenge.description,
                            "challenge_amount" to challenge.amount.numberStripped
                    ))
                }

                // Summen Ausgabe
                sponsorMap.put("sponsor_id", sponsor.account.id!!)
                sponsorMap.put("sponsor_firstname", sponsor.firstname!!)
                sponsorMap.put("sponsor_lastname", sponsor.lastname!!)
                sponsorMap.put("sponsor_email", sponsor.email)
                sponsorMap.put("sponsor_street", sponsor.address!!.street)
                sponsorMap.put("sponsor_housenumber", sponsor.address!!.housenumber)
                sponsorMap.put("sponsor_zipcode", sponsor.address!!.zipcode)
                sponsorMap.put("sponsor_city", sponsor.address!!.city)
                sponsorMap.put("sponsorings", sponsoringsMap)
                sponsorMap.put("challenges", challengesMap)
                sponsorMap.put("sponsorings_sum", sponsoringAmount)
                sponsorMap.put("challenges_sum", challengeAmount)

                registeredSum = registeredSum.add(sponsoringAmount).add(challengeAmount)
                registeredChallengeSum = registeredChallengeSum.add(challengeAmount)
                registeredSponsorSum = registeredSponsorSum.add(sponsoringAmount)

                registerdSponsorsData.add(sponsorMap)
            }

            print(".")
        }

        println()
        println("registeredSum: ${registeredSum.toPlainString()}")
        println("registeredChallengeSum: ${registeredChallengeSum.toPlainString()}")
        println("registeredSponsorSum: ${registeredSponsorSum.toPlainString()}")

        return registerdSponsorsData
    }
}
