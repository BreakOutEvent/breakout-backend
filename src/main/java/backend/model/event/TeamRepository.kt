package backend.model.event

import org.springframework.data.repository.CrudRepository

interface TeamRepository : CrudRepository<Team, Long> {
}