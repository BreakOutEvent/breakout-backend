package backend.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestOperations
import org.springframework.web.client.RestTemplate

@Configuration
open class GeneralConfiguration {
    @Bean
    open fun restOperations(): RestOperations {
        return RestTemplate()
    }
}
