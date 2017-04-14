package backend.model.cache

import backend.exceptions.CacheNonExistentException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class CacheServiceImpl : CacheService {

    private val cacheRepository: CacheRepository

    @Autowired
    constructor(cacheRepository: CacheRepository) {
        this.cacheRepository = cacheRepository
    }

    override fun updateCache(key: String, data: Any) {
        val cache = cacheRepository.findOneByCacheKey(key)
        when (cache) {
            null -> cacheRepository.save(Cache(key, data))
            else -> {
                cache.cacheData = data
                cacheRepository.save(cache)
            }
        }
    }

    override fun getCache(key: String): Any {
        val cache = cacheRepository.findOneByCacheKey(key)
        when (cache) {
            null -> throw CacheNonExistentException("No Cache for $key found; Maybe generated now")
            else -> return cache.cacheData
        }
    }
}

