package backend.model.challenges

import org.springframework.data.repository.CrudRepository


interface ChallengeRepository : CrudRepository<Challenge, Long> {
    fun findByTeamId(teamId: Long): Iterable<Challenge>
}
