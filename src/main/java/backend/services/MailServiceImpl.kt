package backend.services

import backend.model.misc.Email
import backend.model.misc.EmailRepository
import backend.util.Profiles.PRODUCTION
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.util.concurrent.Callable
import java.util.concurrent.Executors

@Service
@Profile(PRODUCTION)
class MailServiceImpl : MailService {

    private val url: String
    private val token: String
    private val restTemplate: RestOperations
    private val emailRepository: EmailRepository

    private val logger = LoggerFactory.getLogger(MailServiceImpl::class.java)
    private val pool = Executors.newCachedThreadPool()

    @Autowired
    constructor(restTemplate: RestOperations, configurationService: ConfigurationService, emailRepository: EmailRepository) {
        this.restTemplate = restTemplate
        this.emailRepository = emailRepository
        this.token = configurationService.getRequired("org.breakout.mailer.xauthtoken")
        this.url = configurationService.getRequired("org.breakout.mailer.url")
    }
    override fun sendInvitationEmail(emailAddress: EmailAddress, team: Team) {

            val germanText = """${team.members.first().firstname} ${team.members.first().lastname} möchte mit Dir ein
            Abenteuer bestreiten!<br><br>

            BreakOut ist ein Spendenmarathon, bei dem Geld für das Bildungprojekt Jambo Bukoba e.V. gesammelt wird.<br><br>

            In Zweierteams versucht Ihr, euch ab Startschuss binnen 36 Stunden so weit wie möglich von Eurem Startpunkt
            (München, Berlin oder Barcelona) zu entfernen. Dabei gilt es, für das Reisen kein Geld auszugeben – vielmehr
            sammelt Ihr pro zurückgelegtem Kilometer Geld für Jambo Bukoba e.V. Von den Spenden werden Schulprojekte in
            Tansania realisiert.<br>
            Das Konzept folgt damit der Idee eines Spendenmarathons: Im Vorfeld akquiriert Ihr eigene Sponsoren, die dann
            pro gereistem Kilometer einen vorab festgelegten Betrag spenden.<br><br>

            Wenn Du Lust hast bei BreakOut teilzunehmen, klicke auf den Button am Ende der E-Mail.<br><br>

            Du hast Fragen oder benötigst Unterstützung? Schreib uns eine E-Mail an
            <a href=\"event@break-out.org\">event@break-out.org</a>.<br><br>

            Liebe Grüße<br>
            Dein BreakOut-Team""".trimIndent()

            val englishText = """${team.members.first().firstname} ${team.members.first().lastname} is inviting you to join
            the adventure!<br><br>

            BreakOut is a charity travel competition. By participating, you'll raise donations for Jambo Bukoba e.V.,
            an NGO promoting health and education in Tanzanian schools.<br><br>

            In pairs of two, you'll try to travel as far as you can within 36 hours from your respective starting location.
            The challenge: you're not allowed to spend any money on transportation! <br>
            Before the event, you'll find sponsors who will donate a fixed amount per kilometer to support
            Jambo Bukoba..<br><br>

            Challenge accepted? Click on the button at the bottom of this message to join your team. <br><br>

            You have questions or need help with your registration?
            Drop us a message at <a href=\"event@break-out.org\">event@break-out.org</a>.<br><br>

            Your BreakOut Team""".trimIndent()

            val germanSubject = "Einladung zu BreakOut 2017"
            val englishSubject = "Invitation to BreakOut 2017!"

            val buttonText = "Einladung annehmen / Accept invite"
            val buttonUrl = "$host/login?utm_source=backend&utm_medium=email&utm_campaign=invite"
            val campaignCode = "invite"

            val email = Email(
                    to = listOf(emailAddress),
                    subject = mergeEmailSubject(germanSubject, englishSubject),
                    body = mergeEmailBody(germanText, englishText),
                    buttonText = buttonText,
                    buttonUrl = buttonUrl,
                    campaignCode = campaignCode)

            this.send(email)
        }

    override fun userwWillResetPassword(){
      val germanText = """Hallo ${user.firstname ?: ""},<br><br>

      du hast angefordert dein Passwort für BreakOut zurückzusetzen.<br>
      Folge dem Button am Ende der Email um ein neues Passwort zu setzen.<br><br>

      Wenn du diese Email nicht angefordert hast, ignoriere sie einfach.<br><br>

      Liebe Grüße<br>
      Euer BreakOut-Team""".trimIndent()

      val englishText = """Dear ${user.firstname ?: ""},<br><br>

      We received a request to change your password.<br>
      Please click on the button below to set a new password.<br><br>

      If you did not make this request, just ignore this email.<br><br>

      Best regards<br>
      Your BreakOut-Team""".trimIndent()

      val germanSubject = "Zurücksetzen deines BreakOut-Passwortes"
      val englishSubject = "Reset Your BreakOut-Password"
    }

    override fun challengeWasWithdrawn(){
      var germanText = """Hallo Team ${challenge.team!!.name}, <br><br>

      eine Challenge wurde zurückgezogen, vielleicht wollt ihr mit dem Sponsor
      noch einmal darüber reden.<br>
      Du hast Fragen oder benötigst Unterstützung? Schreib uns eine E-Mail an
      <a href=\"event@break-out.org\">event@break-out.org</a>.<br><br>

      Liebe Grüße<br>
      Euer BreakOut-Team""".trimIndent()

      var englishText = """Dear Team ${challenge.team!!.name},<br><br>

      unfortunately a sponsor withdrew a challenge. Perhaps you might want to
      contact him or her to find out what's going. <br>
      Do you have any questions or do you need support? Feel free to shoot us
      an email at  <a href=\"event@break-out.org\">event@break-out.org</a>.<br><br>

      Best regards<br>
      Your BreakOut-Team""".trimIndent()

      val germanSubject = "BreakOut 2017 - Rückzug einer Challenge!"
      val englishSubject = "BreakOut 2017 - Challenge Withdrawal!"
    }

    override fun challendeWasCreated(){
      var germanText = """Hallo Team ${team.name}, <br><br>

      Euch wurde eine Challenge gestellt!<br><br>

      \"$description\", bei Erfüllung sammelt Ihr
      ${amount.numberStripped.toPlainString()}€ an zusätzlichen Sponsorengeldern.<br>
      Du hast Fragen oder benötigst Unterstützung? Schreib uns eine E-Mail an
      <a href=\"event@break-out.org\">event@break-out.org</a>.<br><br>

      Liebe Grüße<br>
      Euer BreakOut-Team""".trimIndent()

      var englishText = """Dear Team ${team.name},<br><br>

      you just received a new challenge! <br><br>

      \"$description\". After mastering this challenge your team will receive an
      additional amount of ${amount.numberStripped.toPlainString()}€ for
      Jambo Bukoba.<br>
      Do you have any questions or do you need support? Feel free to shoot us an
      email at  <a href=\"event@break-out.org\">event@break-out.org</a>.<br><br>

      Best regards<br>
      Your BreakOut-Team""".trimIndent()

      val germanSubject = "Eine Challenge wurde gestellt!"
      val englishSubject = "BreakOut 2017 - New Challenge For Your Team!"

    }

    override fun sponsorWasWithdrawn(){
      var germanText = """Hallo Team ${sponsoring.team!!.name}, <br><br>

      ein Sponsoring wurde zurückgezogen, vielleicht wollt ihr mit dem Sponsor
      noch einmal darüber reden.<br>
      Du hast Fragen oder benötigst Unterstützung? Schreib uns eine E-Mail an
      <a href=\"event@break-out.org\">event@break-out.org</a>.<br><br>

      Liebe Grüße<br>
      Euer BreakOut-Team""".trimIndent()

      var englishText = """Dear Team ${sponsoring.team!!.name} <br><br>

      unfortunately a sponsor withdrew their sponsorship for your journey.
      Perhaps you might want to contact him or her to find out what's going on. <br>
      Do you have any questions or do you need support? Feel free to shoot us an
      email at  <a href=\"event@break-out.org\">event@break-out.org</a>.<br><br>

      Best regards<br>
      Your BreakOut-Team""".trimIndent()

      val germanSubject = "BreakOut 2017 - Rückzug eines Sponsorings!"
      val englishSubject = "BreakOut 2017 - Sponsorship Withdrawal!"
    }

    override fun sponsorWasAdded(){
      var germanText = """Hallo Team ${team.name}, <br><br>

      Euch wurde ein Sponsoring hinzugefügt!<br><br>

      Je Kilometer den Ihr zurücklegt erhaltet Ihr
      ${amountPerKm.numberStripped.toPlainString()}€ an zusätzlichen
      Sponsorengeldern, mit einem Limit von maximal
      ${limit.numberStripped.toPlainString()}€.<br>
      Du hast Fragen oder benötigst Unterstützung? Schreib uns eine E-Mail an
      <a href=\"event@break-out.org\">event@break-out.org</a>.<br><br>

      Liebe Grüße<br>
      Euer BreakOut-Team""".trimIndent()

      var englishText = """Dear team ${team.name}, <br><br>

      There is a new sponsorship for your journey!<br><br>

      Your team will receive an additional amount of
      ${amountPerKm.numberStripped.toPlainString()}€ per travelled kilometer
      with a maximum limit of ${limit.numberStripped.toPlainString()}€
      for Jambo Bukoba.<br>
      Do you have any questions or do you need support? Feel free to shoot us an
      email at  <a href=\"event@break-out.org\">event@break-out.org</a>.<br><br>

      Best regards<br>
      Your BreakOut-Team""".trimIndent()

      val germanSubject = "BreakOut 2017 - New Sponsorship!"
      val englishSubject = "BreakOut 2017 - Neues Sponsoring hinzugefügt!"
    }

    override fun teamHasNotPaidInFull(){
      TODO("Überweisungszweck einfügen!")

      var germanText = """Liebes Team ${team.name},<br><br>

      vielen Dank, dass Ihr Euch für den BreakOut 2017 angemeldet habt. Um Eure
      Anmeldung abzuschließen, müsst Ihr noch 60€ Teilnahmegebühr überweisen.
      Solange Ihr keine Teilnahmegebühr überwiesen habt, können wir Euer Team nicht
      freischalten. Bitte überweist das Geld bis spätestens 8. Mai, damit wir euch
      mit einem T-Shirt und Starterkit für Eure Reise ausstatten können.<br><br>

      Bitte überweist eure Teamgebühr an folgendes Konto:<br>
                                Kontoinhaber: Daria Brauner<br>
                                IBAN: DE60700222000072708326<br>
                                BIC: FDDODEMMXXX<br>
                                Überweisungszweck: ????<br>
                                Betrag: 60,00€<br><br>

      Wenn ihr lieber Paypal nutzt, sendet bitte 60€ unter Angabe eurer
      Teamnummer an anmeldung@break-out.org.<br><br>

      Ihr möchtet doch nicht mehr teilnehmen? Schade! Bitte gebt uns kurz unter
      event@break-out.org Bescheid.<br><br>

      Liebe Grüße<br>
      Euer BreakOut-Team""".trimIndent()

      var englishText = """Dear Team ${team.name},<br><br>

      Thank you for signing up for BreakOut 2017. To complete your registration,
      please transfer your registration fee as soon as possible. We can only activate
      your team once we've received the registration fee. Please transfer the fee by
      latest May 8th so that we can equip you with a t-shirt and your starter kit.<br><br>

      Please transfer the registration fee to the following account:<br>
                                Account owner: Daria Brauner<br>
                                IBAN: DE60700222000072708326<br>
                                BIC: FDDODEMMXXX<br>
                                Purpose of transfer: ????????<br>
                                Amount: 60.00€<br><br>

      If you prefer Paypal, please send 60.00€ to anmeldung@break-out.org,
      indicating your team number.<br><br>

      You have decided not to participate in BreakOut 2017? Please let us know by
      sending a message to event@break-out.org <br><br>

      Your BreakOut Team""".trimIndent()

      val germanSubject = "BreakOut 2017 - Bitte bezahlt Eure Teilnahmegebühr!"
      val englishSubject = "BreakOut 2017 - Please pay your registration fee!"
    }

    override fun teamIsNotComplete(){
    TODO("Link einfügen!")
    var germanText = """Hallo ${firstname},<br><br>

    vielen Dank, dass Du dich bei BreakOut angemeldet hast. Um beim BreakOut 2017
    dabei sein zu können, brauchst du noch einen Teampartner. Folge diesem LINK,
    um jemanden in Dein Team einzuladen.<br><br>

    Alle Deine Freunde sind schon verplant? Kein Problem. Schreib uns einfach
    unter event@break-out.org, dass du noch einen Teampartner suchst.<br><br>

    Liebe Grüße<br>
    Dein BreakOut-Team""".trimIndent()

    var englishText = """Hello ${firstname},<br><br>

    thank you for signing up for BreakOut. To participate at BreakOut 2017, you
    still need another team member. Click on this LINK to invite somebody to join
    your team.<br><br>

    All your friends are busy during BreakOut 2017? No problem! Just send a message
    to event@break-out.org and let us know that you're still looking for a
    team partner. <br><br>

    Your BreakOut Team""".trimIndent()

    val germanSubject = "BreakOut 2017 - vervollständige Dein Team!"
    val englishSubject = "BreakOut 2017 - add a team member!"
    }


    
    override fun send(email: Email, saveToDb: Boolean) {
        val headers = HttpHeaders().apply {
            set("Content-Type", "application/json;charset=utf-8")
            set("X-AUTH-TOKEN", token)
        }
        val body = ObjectMapper().writeValueAsString(email)
        val request = HttpEntity<String>(body, headers)

        logger.info("Content of email: $body")

        try {
            val sendurl = getSendUrl(url)
            logger.info("sending mail via: $sendurl")
            restTemplate.exchange(sendurl, HttpMethod.POST, request, String::class.java)
            if (saveToDb) {
                email.isSent = true
                emailRepository.save(email)
            }
        } catch (e: Exception) {
            logger.error(e.message)
            email.isSent = false
            emailRepository.save(email)
            logger.error("Mailer not available at this time, saved mail")
        }
    }

    private fun getSendUrl(baseUrl: String) = UriComponentsBuilder
            .fromHttpUrl(baseUrl)
            .path("send")
            .build().toUriString()

    override fun resendFailed(): Int {
        val failedMails = emailRepository.findByIsSent(false).take(100)
        failedMails.forEach { email ->
            send(email = email, saveToDb = true)
        }
        return failedMails.size
    }

    override fun sendAsync(email: Email, saveToDb: Boolean) {
        pool.submit(Callable {
            send(email, saveToDb)
        })
    }
}
