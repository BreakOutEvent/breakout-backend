package backend.services.mail

import backend.model.challenges.Challenge
import backend.model.event.Team
import backend.model.misc.Email
import backend.model.misc.EmailAddress
import backend.model.payment.SponsoringInvoice
import backend.model.payment.TeamEntryFeeInvoice
import backend.model.sponsoring.Sponsoring
import backend.model.user.Participant
import backend.model.user.User

interface MailService {

    @Deprecated("Use MailSenderService")
    fun send(email: Email, saveToDb: Boolean = false)

    @Deprecated("Use MailSenderService")
    fun resendFailed(): Int

    @Deprecated("Use MailSenderService")
    fun sendAsync(email: Email, saveToDb: Boolean = false)

    fun sendUserHasRegisteredEmail(token: String, user: User)

    fun sendInvitationEmail(emailAddress: EmailAddress, team: Team)

    fun sendTeamIsCompleteEmail(participants: List<Participant>)

    fun sendTeamHasPaidEmail(invoice: TeamEntryFeeInvoice)

    fun userWantsPasswordReset(user: User, token: String)

    fun sendChallengeWasCreatedEmail(challenge: Challenge)

    fun sendChallengeWasWithdrawnEmail(challenge: Challenge)

    fun sendSponsoringWasAddedEmail(sponsoring: Sponsoring)

    fun sendTeamEntryFeePaymentReminderEmail(team: Team)

    fun sendTeamIsNotCompleteReminder(participant: Participant)

    fun sendSponsoringWasWithdrawnEmail(sponsoring: Sponsoring)

    fun sendGeneratedDonationPromiseSponsor(invoice: SponsoringInvoice)
}
