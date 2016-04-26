package backend.model.misc

import org.springframework.data.repository.CrudRepository

interface EmailRepository : CrudRepository<Email, Long> {

}