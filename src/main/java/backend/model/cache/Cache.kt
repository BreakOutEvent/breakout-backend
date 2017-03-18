package backend.model.cache

import java.time.LocalDateTime
import javax.persistence.*

@Entity
class Cache {

    @Id
    @Column(nullable = false, updatable = false)
    lateinit var cacheKey: String

    @Column(columnDefinition = "TEXT")
    lateinit var cacheData: Any


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

    /**
     * private no-args constructor for JPA / Hibernate
     */
    private constructor() : super()

    constructor(key: String, data: Any) {
        this.cacheKey = key
        this.cacheData = data
    }

}
