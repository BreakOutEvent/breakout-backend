package backend.model.challenges

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param


interface ChallengeRepository : CrudRepository<Challenge, Long> {
    fun findByTeamId(teamId: Long): Iterable<Challenge>

    @Query("Select c from Challenge c where c.registeredSponsor.account.id = :id")
    fun findBySponsorAccountId(@Param("id") sponsorId: Long): Iterable<Challenge>

    fun findChallengeProveProjectionById(challengeId: Long): ChallengeProofProjection
}
