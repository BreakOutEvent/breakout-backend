package backend.model.challenges

import backend.model.sponsoring.UnregisteredSponsor
import backend.model.user.Sponsor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param


interface ChallengeRepository : CrudRepository<Challenge, Long> {
    fun findByTeamId(teamId: Long): Iterable<Challenge>

    @Query("Select c from Challenge c where c.registeredSponsor.account.id = :id")
    fun findBySponsorAccountId(@Param("id") sponsorId: Long): Iterable<Challenge>

    fun findChallengeProveProjectionById(challengeId: Long): ChallengeProofProjection

    @Query("select s from Challenge c join c.registeredSponsor as s where c.team.event.id = :eventId")
    fun findAllRegisteredSponsorsWithChallengesAtEvent(@Param("eventId") eventId: Long): Iterable<Sponsor>

    @Query("select s from Challenge c join c.unregisteredSponsor as s where c.team.event.id = :eventId")
    fun findAllUnregisteredSponsorsWithChallengesAtEvent(@Param("eventId") eventId: Long): Iterable<UnregisteredSponsor>
}
