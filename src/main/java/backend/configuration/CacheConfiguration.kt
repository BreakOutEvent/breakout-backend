package backend.configuration

import com.google.common.cache.CacheBuilder
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.guava.GuavaCache
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
@EnableCaching
open class CacheConfiguration {

    @Bean
    open fun cacheManager(): CacheManager {
        val cacheManager = SimpleCacheManager()

        val allChache = GuavaCache("allCache", CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build())
        val singleCache = GuavaCache("singleCache", CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build())

        cacheManager.setCaches(listOf(allChache, singleCache))
        return cacheManager
    }

}