package backend.model

import java.time.LocalDateTime
import javax.persistence.*

@Entity
abstract class BasicEntity {

    @Id
    @GeneratedValue
    @Column(nullable = false, updatable = false)
    var id: Long? = null

    @Column
    var createdAt: LocalDateTime? = null

    @PreUpdate
    @PrePersist
    fun setTimestamps() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now()
        }
    }
}
