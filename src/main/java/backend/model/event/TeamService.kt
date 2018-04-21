package backend.model.event

import backend.model.location.Location
import backend.model.media.Media
import backend.model.misc.EmailAddress
import backend.model.posting.Posting
import backend.model.user.Participant
import backend.model.user.User
import backend.util.data.DonateSums
import org.springframework.security.access.prepost.PreAuthorize

interface TeamService {

    /**
     * Create and save a new team
     *
     * @param creator: The creator and first of the two members of the team
     * @param name: The name of the team
     * @param description: The description of the team
     * @param event: The event a team belongs to
     *
     * @return The newly created Team
     */
    @Throws
    fun create(creator: Participant, name: String, description: String, event: Event, profilePic: Media?): Team

    /**
     * Invite a participant to a team
     *
     * @param emailAddress: The email of participant to be invited
     * @param team: The team to invite the participant to
     */
    @Throws
    @PreAuthorize("#team.isMember(authentication.name)")
    fun invite(emailAddress: EmailAddress, team: Team)

    fun leave(team: Team, participant: Participant)

    /**
     * Persist a team to the database
     *
     * @param team: The team be be saved
     *
     * @return the saved team
     */
    @Throws
    fun save(team: Team): Team

    /**
     * Get Team from database
     *
     * @param id: The id of Team to get
     *
     * @return gotten team object
     */
    fun findOne(id: Long): Team?

    /**
     * Get Team Postings from database
     *
     * @param teamId: The id of Team to get postings for
     *
     * @return gotten postings list
     */
    fun findPostingsById(teamId: Long, page: Int, size: Int): List<Posting>

    /**
     * Get Team Postings from database only including location
     *
     * @param id: The id of Team to get postings for
     *
     * @return gotten postings list
     */
    fun findLocationPostingsById(id: Long): List<Location>

    fun findInvitationsForUser(user: User): List<Invitation>

    fun findInvitationsForUserAndEvent(user: User, eventId: Long): List<Invitation>

    fun findInvitationsByInviteCode(code: String): Invitation?

    fun getDistanceForTeam(teamId: Long): Double

    fun getDistance(teamId: Long): Double

    fun join(participant: Participant, team: Team)

    fun findByEventId(eventId: Long): List<Team>

    fun getDonateSum(teamId: Long): DonateSums

    fun getDonateSum(team: Team): DonateSums

    fun searchByString(search: String): List<Team>

    fun sendEmailsToTeamsWhenEventHasEnded()

    fun findAll(): Iterable<Team>

    fun findAllTeamSummaryProjections(): Iterable<TeamSummaryProjection>

    fun sendEmailsToTeamsWithDonationOverview(event: Event): Int
}
