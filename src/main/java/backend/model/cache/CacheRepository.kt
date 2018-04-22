package backend.model.cache

import org.springframework.data.jpa.repository.JpaRepository

interface CacheRepository : JpaRepository<Cache, Long> {
    fun findOneByCacheKey(key: String): Cache?
}