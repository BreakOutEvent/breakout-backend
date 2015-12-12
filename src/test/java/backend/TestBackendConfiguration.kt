@file:JvmName("SpringBootApplication")
package backend

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication(exclude = arrayOf(SwaggerConfiguration::class))
open class TestBackendConfiguration {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            SpringApplication.run(BackendConfiguration::class.java, *args)
        }
    }
}