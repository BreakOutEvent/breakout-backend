package backend.services.mail

import backend.model.challenges.Challenge
import backend.model.challenges.ChallengeStatus
import backend.model.event.Team
import backend.model.misc.Email
import backend.model.misc.EmailAddress
import backend.model.payment.SponsoringInvoice
import backend.model.payment.TeamEntryFeeInvoice
import backend.model.payment.display
import backend.model.sponsoring.ISponsor
import backend.model.sponsoring.Sponsoring
import backend.model.user.Participant
import backend.model.user.User
import backend.services.ConfigurationService
import org.springframework.stereotype.Service

@Service
class MailServiceImpl(configurationService: ConfigurationService,
                      private val mailSenderService: MailSenderService) : MailService {

    private val host: String = configurationService.getRequired("org.breakout.api.host")

    override fun send(email: Email, saveToDb: Boolean) {
        this.mailSenderService.send(email, saveToDb)
    }

    override fun resendFailed(): Int {
        return this.mailSenderService.resendFailed()
    }

    override fun sendAsync(email: Email, saveToDb: Boolean) {
        this.mailSenderService.sendAsync(email, saveToDb)
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

    override fun userWantsPasswordReset(user: User, token: String) {
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

        val email = Email(
                to = listOf(EmailAddress(user.email)),
                subject = mergeEmailSubject(germanSubject, englishSubject),
                body = mergeEmailBody(germanText, englishText),
                buttonText = "Passwort zurücksetzen / Reset password",
                buttonUrl = "$host/reset/${user.email}/$token?utm_source=backend&utm_medium=email&utm_campaign=pwreset",
                campaignCode = "pwreset")

        this.send(email)
    }

    override fun sendChallengeWasWithdrawnEmail(challenge: Challenge) {
        val germanText = """Hallo Team ${challenge.team!!.name}, <br><br>

            eine Challenge wurde zurückgezogen, vielleicht wollt ihr mit dem Sponsor
            noch einmal darüber reden.<br>
            Du hast Fragen oder benötigst Unterstützung? Schreib uns eine E-Mail an
            <a href=\"event@break-out.org\">event@break-out.org</a>.<br><br>

            Liebe Grüße<br>
            Euer BreakOut-Team""".trimIndent()

        val englishText = """Dear Team ${challenge.team!!.name},<br><br>

            unfortunately a sponsor withdrew a challenge. Perhaps you might want to
            contact him or her to find out what's going. <br>
            Do you have any questions or do you need support? Feel free to shoot us
            an email at  <a href=\"event@break-out.org\">event@break-out.org</a>.<br><br>

            Best regards<br>
            Your BreakOut-Team""".trimIndent()

        val germanSubject = "BreakOut 2017 - Rückzug einer Challenge!"
        val englishSubject = "BreakOut 2017 - Challenge Withdrawal!"

        val email = Email(
                to = challenge.team!!.members.map { EmailAddress(it.email) },
                subject = mergeEmailSubject(germanSubject, englishSubject),
                body = mergeEmailBody(germanText, englishText),
                campaignCode = "challenge_withdrawn")

        this.send(email)
    }

    override fun sendChallengeWasCreatedEmail(challenge: Challenge) {
        val germanText = """Hallo Team ${challenge.team!!.name}, <br><br>

            Euch wurde eine Challenge gestellt!<br><br>

            "${challenge.description}", bei Erfüllung sammelt Ihr
            ${challenge.amount.numberStripped.toPlainString()}€ an zusätzlichen Sponsorengeldern.<br>
            Du hast Fragen oder benötigst Unterstützung? Schreib uns eine E-Mail an
            <a href="event@break-out.org">event@break-out.org</a>.<br><br>

            Liebe Grüße<br>
            Euer BreakOut-Team""".trimIndent()

        val englishText = """Dear Team ${challenge.team!!.name},<br><br>

            you just received a new challenge! <br><br>

            "${challenge.description}". After mastering this challenge your team will receive an
            additional amount of ${challenge.amount.numberStripped.toPlainString()}€ for
            Jambo Bukoba.<br>
            Do you have any questions or do you need support? Feel free to shoot us an
            email at  <a href=\"event@break-out.org\">event@break-out.org</a>.<br><br>

            Best regards<br>
            Your BreakOut-Team""".trimIndent()

        val germanSubject = "Eine Challenge wurde gestellt!"
        val englishSubject = "BreakOut 2017 - New Challenge For Your Team!"

        val email = Email(
                to = challenge.team!!.members.map { EmailAddress(it.email) },
                subject = mergeEmailSubject(germanSubject, englishSubject),
                body = mergeEmailBody(germanText, englishText),
                buttonText = "Challenge annehmen / Accept challenge",
                buttonUrl = "$host/settings/sponsoring?utm_source=backend&utm_medium=email&utm_content=intial&utm_campaign=accept_challenge",
                campaignCode = "accept_challenge")

        this.send(email)
    }

    override fun sendSponsoringWasWithdrawnEmail(sponsoring: Sponsoring) {
        val germanText = """Hallo Team ${sponsoring.team!!.name}, <br><br>

            ein Sponsoring wurde zurückgezogen, vielleicht wollt ihr mit dem Sponsor noch einmal darüber reden.<br>
            Du hast Fragen oder benötigst Unterstützung? Schreib uns eine E-Mail an
            <a href=\"event@break-out.org\">event@break-out.org</a>.<br><br>

            Liebe Grüße<br>
            Euer BreakOut-Team""".trimIndent()

        val englishText = """Dear Team ${sponsoring.team!!.name} <br><br>

            unfortunately a sponsor withdrew their sponsorship for your journey.
            Perhaps you might want to contact him or her to find out what's going on. <br>
            Do you have any questions or do you need support? Feel free to shoot us an
            email at  <a href=\"event@break-out.org\">event@break-out.org</a>.<br><br>

            Best regards<br>
            Your BreakOut-Team""".trimIndent()

        val germanSubject = "BreakOut 2017 - Rückzug eines Sponsorings!"
        val englishSubject = "BreakOut 2017 - Sponsorship Withdrawal!"

        val email = Email(
                to = sponsoring.team!!.members.map { EmailAddress(it.email) },
                subject = mergeEmailSubject(germanSubject, englishSubject),
                body = mergeEmailBody(germanText, englishText),
                campaignCode = "sponsoring_withdrawn")

        this.send(email)
    }

    override fun sendSponsoringWasAddedEmail(sponsoring: Sponsoring) {
        val germanText = """Hallo Team ${sponsoring.team!!.name}, <br><br>

            Euch wurde ein Sponsoring hinzugefügt!<br><br>

            Je Kilometer den Ihr zurücklegt erhaltet Ihr
            ${sponsoring.amountPerKm.numberStripped.toPlainString()}€ an zusätzlichen
            Sponsorengeldern, mit einem Limit von maximal
            ${sponsoring.limit.numberStripped.toPlainString()}€.<br>
            Du hast Fragen oder benötigst Unterstützung? Schreib uns eine E-Mail an
            <a href=\"event@break-out.org\">event@break-out.org</a>.<br><br>

            Liebe Grüße<br>
            Euer BreakOut-Team""".trimIndent()

        val englishText = """Dear team ${sponsoring.team!!.name}, <br><br>

            There is a new sponsorship for your journey!<br><br>

            Your team will receive an additional amount of
            ${sponsoring.amountPerKm.numberStripped.toPlainString()}€ per travelled kilometer
            with a maximum limit of ${sponsoring.limit.numberStripped.toPlainString()}€
            for Jambo Bukoba.<br>
            Do you have any questions or do you need support? Feel free to shoot us an
            email at  <a href=\"event@break-out.org\">event@break-out.org</a>.<br><br>

            Best regards<br>
            Your BreakOut-Team""".trimIndent()

        val germanSubject = "BreakOut 2017 - New Sponsorship!"
        val englishSubject = "BreakOut 2017 - Neues Sponsoring hinzugefügt!"

        val email = Email(
                to = sponsoring.team!!.members.map { EmailAddress(it.email) },
                subject = mergeEmailSubject(germanSubject, englishSubject),
                body = mergeEmailBody(germanText, englishText),
                campaignCode = "sponsoring_added")

        this.send(email)
    }

    override fun sendTeamEntryFeePaymentReminderEmail(team: Team) {


        val germanText = """Liebes Team ${team.name},<br><br>

            vielen Dank, dass Ihr Euch für den BreakOut 2017 angemeldet habt. Um Eure
            Anmeldung abzuschließen, müsst Ihr noch 60€ Teilnahmegebühr überweisen.
            Solange Ihr keine Teilnahmegebühr überwiesen habt, können wir Euer Team nicht
            freischalten. Bitte überweist das Geld bis spätestens 8. Mai, damit wir euch
            mit einem T-Shirt und Starterkit für Eure Reise ausstatten können.<br><br>

            Bitte überweist eure Teamgebühr an folgendes Konto:<br>
            Kontoinhaber: Daria Brauner<br>
            IBAN: DE60700222000072708326<br>
            BIC: FDDODEMMXXX<br>
            Überweisungszweck: ${team.invoice!!.purposeOfTransfer}<br>
            Betrag: 60,00€<br><br>

            Wenn ihr lieber Paypal nutzt, sendet bitte 60€ unter Angabe eurer
            Teamnummer an anmeldung@break-out.org.<br><br>

            Ihr möchtet doch nicht mehr teilnehmen? Schade! Bitte gebt uns kurz unter
            event@break-out.org Bescheid.<br><br>

            Liebe Grüße<br>
            Euer BreakOut-Team""".trimIndent()

        val englishText = """Dear Team ${team.name},<br><br>

            Thank you for signing up for BreakOut 2017. To complete your registration,
            please transfer your registration fee as soon as possible. We can only activate
            your team once we've received the registration fee. Please transfer the fee by
            latest May 8th so that we can equip you with a t-shirt and your starter kit.<br><br>

            Please transfer the registration fee to the following account:<br>
            Account owner: Daria Brauner<br>
            IBAN: DE60700222000072708326<br>
            BIC: FDDODEMMXXX<br>
            Purpose of transfer: ${team.invoice!!.purposeOfTransfer}<br>
            Amount: 60.00€<br><br>

            If you prefer Paypal, please send 60.00€ to anmeldung@break-out.org,
            indicating your team number.<br><br>

            You have decided not to participate in BreakOut 2017? Please let us know by
            sending a message to event@break-out.org <br><br>

            Your BreakOut Team""".trimIndent()

        val germanSubject = "BreakOut 2017 - Bitte bezahlt Eure Teilnahmegebühr!"
        val englishSubject = "BreakOut 2017 - Please pay your registration fee!"

        val email = Email(
                to = team.members.map { EmailAddress(it.email) },
                subject = mergeEmailSubject(germanSubject, englishSubject),
                body = mergeEmailBody(germanText, englishText),
                campaignCode = "teamentryfee_payment_reminder")

        this.send(email)
    }

    override fun sendTeamIsNotCompleteReminder(participant: Participant) {

        val germanText = """Hallo ${participant.firstname},<br><br>

            vielen Dank, dass Du dich bei BreakOut angemeldet hast. Um beim BreakOut 2017
            dabei sein zu können, brauchst du noch einen Teampartner. Folge diesem LINK,
            um jemanden in Dein Team einzuladen.<br><br>

            Alle Deine Freunde sind schon verplant? Kein Problem. Schreib uns einfach
            unter event@break-out.org, dass du noch einen Teampartner suchst.<br><br>

            Liebe Grüße<br>
            Dein BreakOut-Team""".trimIndent()

        val englishText = """Hello ${participant.firstname},<br><br>

            thank you for signing up for BreakOut. To participate at BreakOut 2017, you
            still need another team member. Click on this LINK to invite somebody to join
            your team.<br><br>

            All your friends are busy during BreakOut 2017? No problem! Just send a message
            to event@break-out.org and let us know that you're still looking for a
            team partner. <br><br>

            Your BreakOut Team""".trimIndent()

        val germanSubject = "BreakOut 2017 - vervollständige Dein Team!"
        val englishSubject = "BreakOut 2017 - add a team member!"

        val email = Email(
                to = listOf(EmailAddress(participant.email)),
                subject = mergeEmailSubject(germanSubject, englishSubject),
                body = mergeEmailBody(germanText, englishText),
                campaignCode = "lonely_participants")

        this.send(email)
    }

    override fun sendUserHasRegisteredEmail(token: String, user: User) {

        val germanText = """Vielen Dank für Dein Interesse an BreakOut 2017.<br><br>

        Zum Schutz Deiner Daten müssen wir sicherstellen, dass diese E-Mail-Adresse Dir gehört. Bitte klicke dazu auf
        den Button am Ende der E-Mail.<br><br>

        Du hast Dich nicht für BreakOut angemeldet? Dann ignoriere diese E-Mail einfach.<br><br>

        Du hast Fragen oder benötigst Unterstützung? Schreib uns eine E-Mail an
        <a href='mailto:event@break-out.org'>event@break-out.org</a>.<br><br>

        Liebe Grüße<br>
        Dein BreakOut-Team""".trimIndent()


        val englishText = """Thank you for your interest in BreakOut 2017!<br><br>

         To protect your data we need to make sure this e-mail address belongs to you. Please click on the button on
         the bottom of this message to confirm your e-mail address.<br><br>

         You did not sign up for BreakOut? Please just ignore this e-mail.<br><br>

         You have questions or need support with your registration? Drop us a message at
         <a href='mailto:event@break-out.org'>event@break-out.org</a>.<br><br>

         Your BreakOut Team""".trimIndent()

        val germanSubject = "Willkommen bei BreakOut 2017!"
        val englishSubject = "Welcome to BreakOut 2017!"

        val subject = mergeEmailSubject(germanSubject, englishSubject)
        val body = mergeEmailBody(germanText, englishText)

        val buttonText = "Email-Adresse bestätigen / Confirm email address"
        val buttonUrl = "$host/activation/$token?utm_source=backend&utm_medium=email&utm_campaign=confirm"
        val campaignCode = "confirm"

        val mail = Email(
                to = listOf(EmailAddress(user.email)),
                subject = subject,
                body = body,
                buttonText = buttonText,
                buttonUrl = buttonUrl,
                campaignCode = campaignCode)

        this.send(mail)
    }

    override fun sendTeamIsCompleteEmail(participants: List<Participant>) {
        val first = participants.first()
        val second = participants.last()

        sendTeamIsCompleteEmailForOneParticipant(first, second)
        sendTeamIsCompleteEmailForOneParticipant(second, first)
    }

    private fun sendTeamIsCompleteEmailForOneParticipant(first: Participant, second: Participant) {

        val purposeOfTransfer = first.getCurrentTeam()?.invoice?.purposeOfTransfer
                ?: throw Exception("User has no team with invoice")

        val germanText = """Hallo ${first.firstname},<br><br>

        Herzlichen Glückwunsch! Du bist jetzt mit ${second.firstname} in einem Team und Euer Team ist damit
        vollständig.<br>

        Um Eure Anmeldung abzuschließen, müsst Ihr nur noch die Teilnahmegebühr von 60€ pro Team bis spätestens
        8. Mai überweisen. In der Gebühr ist ein Deposit von 20€ enthalten. Wenn ihr 100€ Spenden gesammelt habt
        (keine Sorge, das schafft ihr locker!), wird euch das Deposit nach dem Event zurücküberwiesen.<br><br>

        Bitte überweist eure Teamgebühr an folgendes Konto:<br><br>

        Kontoinhaber: Daria Brauner<br>
        IBAN: DE60700222000072708326<br>
        BIC: FDDODEMMXXX<br>
        Überweisungszweck: $purposeOfTransfer<br>
        Betrag: 60,00€<br><br>

        Wenn ihr lieber Paypal nutzt, sendet bitte 60€ unter Angabe eurer Teamnummer an
        <a href="mailto:anmeldung@break-out.org">anmeldung@break-out.org</a>.<br><br>

        Liebe Grüße<br>
        Euer BreakOut-Team""".trimIndent()

        val englishText = """Hello ${first.firstname},<br><br>

        Congratulations! ${second.firstname} will join you for BreakOut 2017 - your team is now complete.<br>
        To complete your registration, please transfer the registration fee of 60€ per team by May 8th. This fee
        includes a deposit of 20€. When your team has raised 100€ of donations
        (no worries, you'll definitely raise more than 100€ :) ! ), we will transfer the deposit back to your
        account.<br><br>

        Please transfer the registration fee to the following account:<br><br>

        Account owner: Daria Brauner<br>
        IBAN: DE60700222000072708326<br>
        BIC: FDDODEMMXXX<br>
        Purpose of transfer: $purposeOfTransfer<br>
        Amount: 60.00€<br><br>

        If you prefer Paypal, please send 60.00€ to
        <a href="mailto:anmeldung@break-out.org">anmeldung@break-out.org</a>, indicating your team number.<br><br>

        Your BreakOut Team"""

        val germanSubject = "BreakOut 2017 - Ein letzter Schritt zur Anmeldung!"
        val englishSubject = "BreakOut 2017 - One final step to your registration!"

        val url = "$host/join-team-success?utm_source=backend&utm_medium=email&utm_content=intial&utm_campaign=payment"

        val email = Email(
                to = listOf(EmailAddress(first.email)),
                subject = mergeEmailSubject(germanSubject, englishSubject),
                body = mergeEmailBody(germanText, englishText),
                buttonText = "Jetzt zahlen / Pay now",
                buttonUrl = url,
                campaignCode = "payment"
        )

        this.send(email)
    }

    override fun sendTeamHasPaidEmail(invoice: TeamEntryFeeInvoice) {
        val germanText = """Liebes Team ${invoice.team!!.name},<br><br>

        Eure Startgebühr ist vollständig bei uns eingegangen. Eure Anmelung für den BreakOut 2017 ist damit
        abgeschlossen! Jetzt geht's an die Sponsorensuche. Infos dazu findet ihr hier:
        <a href="https://break-out.org/next-steps">https://break-out.org/next-steps</a> <br>
        Über alles weitere halten wir Euch per E-Mail auf dem Laufenden! Schaut außerdem regelmäßig auf unserer
        Facebookseite vorbei für großartige Gewinnspiele und die neuesten Neuigkeiten rund um BreakOut 2017:
        https://www.facebook.com/breakoutmuenchen/ <br>
        Bis zum 25. Mai - wir freuen uns auf Euch!<br><br>

        Liebe Grüße<br>
        Euer BreakOut-Team"""

        val englishText = """Dear Team ${invoice.team!!.name},<br><br>

        We've received your registration fee - thank you! Your registration for BreakOut2017 is completed successfully.
        Now it's time to find sponsors.You can get more information on how to acquire sponsors on our website:
        <a href="https://break-out.org/next-steps">https://break-out.org/next-steps</a> <br>
        We'll send you more information about the event via e-mail. Check out our Facebook page for the latest news and
        fun competitions.<br>
        We're excited to see you at BreakOut 2017 on May 25th!<br><br>

        Your BreakOut Team"""

        val germanSubject = "BreakOut 2017 Anmeldung erfolgreich!"
        val englishSubject = "BreakOut 2017 Registration successful"

        val mail = Email(
                to = invoice.team!!.members.map { EmailAddress(it.email) },
                subject = mergeEmailSubject(germanSubject, englishSubject),
                body = mergeEmailBody(germanText, englishText),
                buttonUrl = "https://break-out.org/next-steps?utm_source=backend&utm_medium=email&utm_content=full&utm_campaign=payment",
                buttonText = "Nächste Schritte / Next steps",
                campaignCode = "payment_full")
        this.send(mail)
    }

    override fun sendTeamWithDonationOverviewEmail(team: Team) {
        val raisedAmount = team.raisedAmountFromChallenges().add(team.raisedAmountFromSponsorings())
        val numberFulfilled = team.challenges.filter { it.status == ChallengeStatus.WITH_PROOF }.count()
        val distance = team.getCurrentDistance()

        val germanText = """
            |Liebes Team ${team.name},
            |
            |Ihr seid der Wahnsinn! Ihr habt dieses Jahr eine Strecke von $distance km zurückgelegt, dabei $numberFulfilled Challenges erfüllt und $raisedAmount € an Spendenversprechen generiert. Chapeau!
            |Bei Challenges, für die das Spendenversprechen 0€ beträgt, wurde die Challenge leider während des BreakOuts 2017 nicht erfüllt.
            |
            |Diese Spenden setzen sich wie folgt zusammen:
            |
            |${team.toEmailOverview()}
            |
            |Wenn eure Kilometerspenden auf "ACCEPTED" stehen, habt ihr sie akzeptiert und sie werden eurem Spendenversprechen angerechnet.
            |Wenn eure Kilometerspenden auf "WITHDRAWN" stehen, dann hat euer Sponsor dieses Spendenversprechen zurückgezogen.
            |
            |Bitte erinnert eure Sponsoren an die Bezahlung des Spendenversprechens bis 6. Juni, damit euer Geld bis zur Siegerehrung da ist.
            |
            |Haben wir schon erwähnt, wie großartig Ihr seid ;-)
            |
            |Cheerio,
            |Euer BreakOut Team """.trimMargin("|").addHtmlNewlines()

        val englishText = """
            |Dear Team ${team.name},
            |
            |You are madness! You covered a distance of $distance km this year, fulfilled $numberFulfilled Challenges and generated a donations promise of $raisedAmount. Chapeau!
            |If your donation promise for a given challenge is 0€, you have unfortunately not mastered the challenge during BreakOut 2017.
            |
            |These donations are made up as follows:
            |
            |${team.toEmailOverview()}
            |
            |Please remind your sponsors of the payment of the donation by June 6th so that we'll receive the payment by the victory party.
            |
            |Have we already mentioned how awesome you are? ;-)
            |
            |Cheerio,
            |Your BreakOut Team""".trimMargin("|").addHtmlNewlines()

        val germanSubject = "So viele Spenden habt ihr gesammelt"
        val englishSubject = "You have raised so many donations"
        val email = Email(
                to = team.members.map { EmailAddress(it.email) },
                subject = mergeEmailSubject(germanSubject, englishSubject),
                body = mergeEmailBody(germanText, englishText),
                campaignCode = "team_generated_donations"
        )
        mailSenderService.send(email)
    }

    override fun sendInvoiceReminderEmailsToTeam(team: Team, invoice: List<SponsoringInvoice>) {
        val germanText = """
        |Eure Spendenübersicht
        |
        |Liebes Team ${team.name},
        |
        |Wir hoffen, ihr seid gut wieder im Alltag angekommen.
        |
        |Unten findet ihr eine Auflistung aller Sponsoren, bei denen noch nicht das vollständige Spendenversprechen bei uns
        |eingegangen ist. Bitte erinnert Eure Spender nochmal, die noch offenen Beträge zu überweisen.
        |
        |Bitte beachtet, dass eure Sponsoren auch mehrere Teams unterstützt haben könnten. Sponsoren, die hier gelistet sind, können auch noch offene Spendenversprechen bei anderen Teams haben und daher hier auftauchen.
        |
        |${invoice.foldRight("") { a, b -> b + "${a.sponsor.firstname} ${a.sponsor.lastname} ${buildSponsorCompanyText(a.sponsor)}\n" }}
        |
        |Die offenen Spenden sollten bitte wie folgt überwiesen werden:
        |
        |KONTOINHABER: BreakOut e.V.
        |BANKNAME: Fidor Bank
        |IBAN: DE85700222000020241837
        |BIC: FDDODEMMXXX
        |
        |VERWENDUNGSZWECK: Bitte der Spendenübersicht, die wir euren Sponsoren gesendet haben, entnehmen.
        |
        |Bei Fragen meldet euch gerne unter event@break-out.org.
        |Liebe Grüße,
        |
        |Euer BreakOut-Team""".trimMargin("|").addHtmlNewlines()

        val englishText = """
        |Your donation overview
        |
        |Dear Team ${team.name},
        |
        |We hope you have happily returned to your everyday life.
        |
        |Here you find a listing of all your sponsors where we have not yet received the full donation promise.
        |Please remind your supporters again to transfer their donations.
        |
        |Beware that your sponsors might have supported other teams as well. The sponsors being listed below might also be a result of unpaid donation promises for other teams.
        |
        |${invoice.foldRight("") { a, b -> b + "${a.sponsor.firstname} ${a.sponsor.lastname} ${buildSponsorCompanyText(a.sponsor)}\n" }}
        |
        |The open donations should be transferred to following bank account:
        |
        |Account owner: BreakOut e.V.
        |IBAN: DE85700222000020241837
        |BIC: FDDODEMMXXX
        |Payment referencer: See donation overview we sent each of your sponsors.
        |
        |Please contact us at event@break-out.org if you have any questions.
        |Best regards,
        |
        |Your BreakOut Team""".trimMargin("|").addHtmlNewlines()

        val germanSubject = "Eure Spendenübersicht"
        val englishSubject = "Your donation overview"

        val email = Email(
                to = team.members.map { EmailAddress(it.email) },
                subject = mergeEmailSubject(germanSubject, englishSubject),
                body = mergeEmailBody(germanText, englishText),
                campaignCode = "team_generated_donations"
        )

        mailSenderService.send(email)
    }

    private fun buildSponsorCompanyText(sponsor: ISponsor): String {
        sponsor.company?.let {
            return "- $it"
        }
        return ""
    }

    override fun sendGeneratedDonationPromiseSponsor(invoice: SponsoringInvoice) {

        val germanText = """
            |Sollten Sie bereits eine Email mit Ihrem Spendenversprechen erhalten haben, können Sie diese ignorieren
            |
            |Liebe(r) ${invoice.sponsor.firstname} ${invoice.sponsor.lastname},
            |
            |vielen herzlichen Dank, dass Sie beim BreakOut 2017 ein Team unterstützen! Ihre Spende wird von BreakOut e. V. an Jambo Bukoba e. V. weitergeleitet. So unterstützen Sie den Bau von Regenwassertanks an tansanischen Schulen. Wir bitten Sie herzlich, Ihre Spende bis zum 6. Juni an das unten angegebene Konto zu überweisen, damit wir das Geld rechtzeitig zur Siegerehrung des diesjährigen BreakOuts erhalten.
            |Bei Challenges, für die Ihr Spendenversprechen 0€ beträgt, wurde die Challenge vom Team leider während des BreakOuts 2017 nicht erfüllt.
            |Bitte beachten Sie: wenn Sie Teams in mehreren Städten unterstützt haben, erhalten Sie pro Stadt eine Email mit der Auflistung Ihres Spendenversprechens für diese Stadt.
            |
            |Hier eine Aufschlüsselung Ihres Spendenversprechens:
            |
            |${invoice.toEmailOverview()}
            |
            |
            |BETRAG: ${invoice.amount.display()}
            |VERWENDUNGSZWECK: ${invoice.purposeOfTransfer}
            |
            |KONTOINHABER: BreakOut e.V.
            |BANKNAME: Fidor Bank
            |IBAN: DE85700222000020241837
            |BIC: FDDODEMMXXX
            |
            |Besonders wichtig ist der Verwendungszweck, denn nur so können Ihre Spenden dem richtigen Team zugeordnet werden. Darüber hinaus können wir Ihnen nur bei Hinterlegung Ihrer Adresse in Ihrem Account eine offizielle Zuwendungsbescheinigung zusenden. Bei Rückfragen zur Adressänderung wenden Sie sich bitte an event@break-out.org.
            |Sie erhalten am Ende des Kalenderjahres eine Zuwendungsbescheinigung von uns, wenn Sie einen Betrag von mehr als 200€ gespendet haben. Bei Beträgen von weniger als 200€ reicht es aus bei Ihrem Finanzamt eine vereinfachte Spendenbescheinigung einzureichen. Bitte beachten Sie dazu das Dokument unter folgendem <a href="http://assets.contentful.com/i8fp6rw03mps/2LEqetuxOMCc4wciskMgwO/9e8448c32314de24cd099888a0ae3125/VereinfachterZuwendungsnachweis.pdf">Link</a> [1].
            |
            |Bei Fragen wenden Sie sich gerne an die von Ihnen unterstützten Teams oder direkt an uns unter event@break-out.org.
            |
            |Herzlichen Dank für Ihre Unterstützung.
            |
            |Wir wünschen Ihnen eine schöne Woche,
            |Ihr BreakOut-Team
            |
            |[1] http://assets.contentful.com/i8fp6rw03mps/2LEqetuxOMCc4wciskMgwO/9e8448c32314de24cd099888a0ae3125/VereinfachterZuwendungsnachweis.pdf
            """.trimMargin("|").addHtmlNewlines()

        val englishText = """
            |Should you have received an email stating your donation promise before, please ignore this email
            |
            |Dear ${invoice.sponsor.firstname} ${invoice.sponsor.lastname},
            |
            |Many, many thanks for supporting a team during BreakOut 2017! Your donation will allow Jambo Bukoba build water tanks at Tanzanian schools and make an important contribution to improving students' lives. We would kindly ask you to transfer your donation by June 6th so that we will receive the payment in time for our awards party.
            |If your donation promise for a given challenge is 0€, the team has unfortunately not mastered the challenge during BreakOut 2017.
            |If you supported teams in different cities, you will receive on email per city showing your donation promise for the given city.
            |
            |${invoice.toEmailOverview()}
            |
            |
            |Amount: ${invoice.amount.display()}
            |Payment reference: ${invoice.purposeOfTransfer}
            |
            |Account holder: BreakOut e.V.
            |Bank: Fidor Bank
            |IBAN: DE85700222000020241837
            |BIC: FDDODEMMXXX
            |
            |Please pay close attention to using the correct payment reference, because we can only assign your donation to the right team with the correct purpose. In addition, we can only send you an official donation receipt when you've saved your address in your BreakOut account. Feel free to contact us under event@break-out.org if you need any assistance. We will send you an official donation receipt by the end of 2017 if your donation amounts to more than 200€. Under German law, it is otherwise sufficient for you to provide a simplified donation receipt. For more info, please consult this <a href="|[1] http://assets.contentful.com/i8fp6rw03mps/2LEqetuxOMCc4wciskMgwO/9e8448c32314de24cd099888a0ae3125/VereinfachterZuwendungsnachweis.pdf">Link</a>.
            |
            |If you have any questions, please do not hesitate to contact the team you are supporting or us at event@break-out.org.
            |
            |Thank you so much for your support.
            |
            |We wish you a great week,
            |Your BreakOut team
            |[1] http://assets.contentful.com/i8fp6rw03mps/2LEqetuxOMCc4wciskMgwO/9e8448c32314de24cd099888a0ae3125/VereinfachterZuwendungsnachweis.pdf
            """.trimMargin("|").addHtmlNewlines()

        val email = Email(
                to = invoice.getContactEmails(),
                subject = mergeEmailSubject("Ihr Spendenversprechen für BreakOut 2017", "Your donation promise for BreakOut 2017"),
                body = mergeEmailBody(germanText, englishText))

        mailSenderService.send(email)

    }

    override fun sendGeneratedDonationPromiseReminderSponsor(invoice: SponsoringInvoice) {


        val germanText = """
            |Liebe(r) ${invoice.sponsor.firstname} ${invoice.sponsor.lastname},
            |
            |vielen herzlichen Dank, dass Sie beim BreakOut 2017 ein Team unterstützen! Ihre Spende wird von BreakOut e. V. an Jambo Bukoba e. V. weitergeleitet. So unterstützen Sie den Bau von Regenwassertanks an tansanischen Schulen. Daher möchten wir Sie gern daran erinnern, Ihr Spendenversprechen baldmöglichst einzulösen.
            |Sollten Sie Ihre Überweisung bereits veranlasst haben, können Sie diese Email einfach ignorieren.
            |
            |Bei Challenges, für die Ihr Spendenversprechen 0€ beträgt, wurde die Challenge vom Team leider während des BreakOuts 2017 nicht erfüllt.
            |Bitte beachten Sie: wenn Sie Teams in mehreren Städten unterstützt haben, erhalten Sie pro Stadt eine Email mit der Auflistung Ihres Spendenversprechens für diese Stadt.
            |
            |Hier eine Aufschlüsselung Ihres Spendenversprechens:
            |
            |${invoice.toEmailOverview()}
            |
            |
            |BETRAG: ${invoice.amount.display()}
            |VERWENDUNGSZWECK: ${invoice.purposeOfTransfer}
            |
            |KONTOINHABER: BreakOut e.V.
            |BANKNAME: Fidor Bank
            |IBAN: DE85700222000020241837
            |BIC: FDDODEMMXXX
            |
            |Besonders wichtig ist der Verwendungszweck, denn nur so können Ihre Spenden dem richtigen Team zugeordnet werden. Darüber hinaus können wir Ihnen nur bei Hinterlegung Ihrer Adresse in Ihrem Account eine offizielle Zuwendungsbescheinigung zusenden. Bei Rückfragen zur Adressänderung wenden Sie sich bitte an event@break-out.org.
            |Sie erhalten am Ende des Kalenderjahres eine Zuwendungsbescheinigung von uns, wenn Sie einen Betrag von mehr als 200€ gespendet haben. Bei Beträgen von weniger als 200€ reicht es aus bei Ihrem Finanzamt eine vereinfachte Spendenbescheinigung einzureichen. Bitte beachten Sie dazu das Dokument unter folgendem <a href="http://assets.contentful.com/i8fp6rw03mps/2LEqetuxOMCc4wciskMgwO/9e8448c32314de24cd099888a0ae3125/VereinfachterZuwendungsnachweis.pdf">Link</a> [1].
            |
            |Bei Fragen wenden Sie sich gerne an die von Ihnen unterstützten Teams oder direkt an uns unter event@break-out.org.
            |
            |Herzlichen Dank für Ihre Unterstützung.
            |
            |Wir wünschen Ihnen eine schöne Woche,
            |Ihr BreakOut-Team
            |
            |[1] http://assets.contentful.com/i8fp6rw03mps/2LEqetuxOMCc4wciskMgwO/9e8448c32314de24cd099888a0ae3125/VereinfachterZuwendungsnachweis.pdf
            """.trimMargin("|").addHtmlNewlines()

        val englishText = """
            |
            |Dear ${invoice.sponsor.firstname} ${invoice.sponsor.lastname},
            |
            |Many, many thanks for supporting a team during BreakOut 2017! Your donation will allow Jambo Bukoba build water tanks at Tanzanian schools and make an important contribution to improving students' lives. We would like to kindly remind you to fulfill your donation promise as soon as possible. If you have already transferred your donation, you can simply ignore this email.
            |If your donation promise for a given challenge is 0€, the team has unfortunately not mastered the challenge during BreakOut 2017.
            |If you supported teams in different cities, you will receive on email per city showing your donation promise for the given city.
            |
            |${invoice.toEmailOverview()}
            |
            |
            |Amount: ${invoice.amount.display()}
            |Payment reference: ${invoice.purposeOfTransfer}
            |
            |Account holder: BreakOut e.V.
            |Bank: Fidor Bank
            |IBAN: DE85700222000020241837
            |BIC: FDDODEMMXXX
            |
            |Please pay close attention to using the correct payment reference, because we can only assign your donation to the right team with the correct purpose. In addition, we can only send you an official donation receipt when you've saved your address in your BreakOut account. Feel free to contact us under event@break-out.org if you need any assistance. We will send you an official donation receipt by the end of 2017 if your donation amounts to more than 200€. Under German law, it is otherwise sufficient for you to provide a simplified donation receipt. For more info, please consult this <a href="http://assets.contentful.com/i8fp6rw03mps/2LEqetuxOMCc4wciskMgwO/9e8448c32314de24cd099888a0ae3125/VereinfachterZuwendungsnachweis.pdf">Link</a>.[1]
            |
            |If you have any questions, please do not hesitate to contact the team you are supporting or us at event@break-out.org.
            |
            |Thank you so much for your support.
            |
            |We wish you a great week,
            |Your BreakOut team
            |[1] http://assets.contentful.com/i8fp6rw03mps/2LEqetuxOMCc4wciskMgwO/9e8448c32314de24cd099888a0ae3125/VereinfachterZuwendungsnachweis.pdf
            """.trimMargin("|").addHtmlNewlines()

        val email = Email(
                to = invoice.getContactEmails(),
                subject = mergeEmailSubject("Ihr Spendenversprechen für BreakOut 2017", "Your donation promise for BreakOut 2017"),
                body = mergeEmailBody(germanText, englishText))

        mailSenderService.send(email)

    }


    private fun mergeEmailBody(germanText: String, englishText: String): String {
        return germanText + "<br><br><hr><br><br>" + englishText
    }

    private fun mergeEmailSubject(germanSubject: String, englishSubject: String): String {
        return germanSubject + " / " + englishSubject
    }
}

fun String.addHtmlNewlines(): String {
    return this.replace("\n", "\n<br />")
}
