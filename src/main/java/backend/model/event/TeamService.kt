package backend.model.event

import backend.model.misc.EmailAddress
import backend.model.user.Participant
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
    fun create(creator: Participant, name: String, description: String, event: Event): Team

    /**
     * Invite a participant to a team
     *
     * @param email: The email of participant to be invited
     * @param team: The team to invite the participant to
     */
    @Throws
    @PreAuthorize("#team.isMember(authentication.name)")
    open fun invite(email: EmailAddress, team: Team)

    /**
     * Persist a team to the database
     *
     * @param team: The team be be saved
     *
     * @return the saved team
     */
    @Throws
    fun save(team: Team): Team
}
