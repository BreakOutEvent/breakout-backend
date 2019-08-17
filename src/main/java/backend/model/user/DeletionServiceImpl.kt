package backend.model.user

import backend.model.challenges.Challenge
import backend.model.challenges.ChallengeRepository
import backend.model.event.TeamRepository
import backend.model.media.Media
import backend.model.event.Team
import backend.model.posting.Posting
import backend.model.sponsoring.Sponsoring
import backend.model.location.Location
import backend.model.location.LocationRepository
import backend.model.media.MediaRepository
import backend.model.messaging.GroupMessage
import backend.model.messaging.GroupMessageRepository
import backend.model.misc.Email
import backend.model.misc.EmailRepository
import backend.model.payment.*
import backend.model.posting.PostingRepository
import backend.model.sponsoring.SponsoringRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeletionServiceImpl @Autowired constructor(private val userRepository: UserRepository,
                                                 private val postingRepository: PostingRepository,
                                                 private val locationRepository: LocationRepository,
                                                 private val sponsoringRepository: SponsoringRepository,
                                                 private val sponsoringInvoiceRepository: SponsoringInvoiceRepository,
                                                 private val teamRepository: TeamRepository,
                                                 private val messageRepository: GroupMessageRepository,
                                                 private val challengeRepository: ChallengeRepository,
                                                 private val emailRepository: EmailRepository) : DeletionService {

    fun delete(email: Email) {
        emailRepository.delete(email)
    }

    fun delete(invoice: SponsoringInvoice) {
        invoice.removeSponsor()
        sponsoringInvoiceRepository.save(invoice)
    }

    fun delete(location: Location) {
        location.uploader = null
        locationRepository.save(location)
    }

    fun delete(groupMessage: GroupMessage, user: User) {
        groupMessage.messages
                .filter { it.creator?.id == user.account.id }
                .forEach {
                    it.text = ""
                    it.creator = null
                }

        groupMessage.users.removeIf { it.account.id == user.account.id }
        messageRepository.save(groupMessage)
    }

    fun delete(sponsoring: Sponsoring) {
        sponsoring.removeSponsor()
        sponsoringRepository.save(sponsoring)
    }

    fun delete(team: Team, user: User) {
        team.members.removeIf { it.account.id == user.account.id }
        teamRepository.save(team)
    }

    fun delete(challenge: Challenge) {
        challenge.removeSponsor()
        challenge.description = ""
        challengeRepository.save(challenge)
    }

    fun deleteComments(posting: Posting, user: User) {
        posting.comments
                .filter { it.user?.account?.id == user.account.id }
                .forEach {
                    it.user = null
                    it.text = ""
                }

        postingRepository.save(posting)
    }

    fun delete(posting: Posting) {
        if (posting.challenge == null && posting.comments.isEmpty()) {
            postingRepository.delete(posting)
        } else {
            posting.media = null
            posting.user = null
            posting.text = ""
            postingRepository.save(posting)
        }
    }

    @Transactional
    fun deleteUserGeneratedData(user: User) {
        user.account.getRole(Sponsor::class)?.let {
            sponsoringInvoiceRepository.findBySponsorId(it.id!!).forEach(::delete)
        }

        sponsoringRepository.findBySponsorAccountId(user.account.id!!).forEach(::delete)
        challengeRepository.findBySponsorAccountId(user.account.id!!).forEach(::delete)

        messageRepository
                .findWhereUserHasSentMessages(user.account.id!!)
                .forEach { delete(it, user) }

        user.account.groupMessages.forEach { delete(it, user) }

        user.account
                .getRole(Participant::class)
                ?.getAllTeams()
                ?.forEach { delete(it, user) }

        postingRepository
                .findAllByUserId(user.account.id!!)
                .forEach(::delete)

        postingRepository
                .findAllLikedByUser(user.account.id!!)
                .forEach {
                    it.unlike(user.account)
                    postingRepository.save(it)
                }

        postingRepository
                .findAllCommentedByUser(user.account.id!!)
                .forEach { deleteComments(it, user) }

        locationRepository
                .findAllByUploaderId(user.account.id!!)
                .forEach(::delete)

        emailRepository
                .findByReceipient(user.account.email)
                .forEach(::delete)
    }

    override fun delete(user: User) {
        deleteUserGeneratedData(user)
        userRepository.delete(user.account)
    }
}