package backend.model.cache

import org.springframework.data.repository.CrudRepository

interface CacheRepository : CrudRepository<Cache, Long> {
    fun findOneByCacheKey(key: String): Cache?
}