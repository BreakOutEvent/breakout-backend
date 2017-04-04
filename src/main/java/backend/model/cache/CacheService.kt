package backend.model.cache

interface CacheService {

    fun updateCache(key: String, data: Any)

    fun getCache(key: String): Any
}

