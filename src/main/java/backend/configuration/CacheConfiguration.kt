import com.google.common.cache.CacheBuilder
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.guava.GuavaCache
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@EnableCaching
@Configuration
class CacheConfiguration {

    @Bean
    fun cacheManager(): CacheManager {
        val cacheManager = SimpleCacheManager()

        val getSingleCache = GuavaCache("singleCache", CacheBuilder.newBuilder().expireAfterAccess(2, TimeUnit.MINUTES).build())
        val getAllCache = GuavaCache("allCache", CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build())

        cacheManager.setCaches(listOf(getSingleCache, getAllCache))
        return cacheManager
    }
}