package backend.model.location

import org.springframework.data.repository.CrudRepository

interface LocationRepository : CrudRepository<Location, Long>
