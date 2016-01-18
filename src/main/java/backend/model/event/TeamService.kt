package backend.model.event

import backend.model.user.Participant

interface TeamService {

    /**
     * Create and save a new team
     *
     * @param creator: The creator and first of the two members of the team
     * @param name: The name of the team
     * @param description: The description of the team
     *
     * @return The newly created Team
     */
    @Throws
    fun create(creator: Participant, name: String, description: String): Team

    /**
     * Invite a participant to a team
     *
     * @param email: The email of participant to be invited
     * @param team: The team to invite the participant to
     */
    @Throws
    open fun invite(email: String, team: Team)

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
