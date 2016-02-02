package backend.model

import java.time.LocalDateTime
import javax.persistence.*

@MappedSuperclass
abstract class BasicEntity {

    @Id
    @GeneratedValue
    @Column(nullable = false, updatable = false)
    var id: Long? = null

    @Column
    var createdAt: LocalDateTime? = null

    @Column
    var updatedAt: LocalDateTime? = null

    @PreUpdate
    @PrePersist
    fun setTimestamps() {
        updatedAt = LocalDateTime.now()
        if (createdAt == null) {
            createdAt = LocalDateTime.now()
        }
    }
}
