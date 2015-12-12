@file:JvmName("BackendConfiguration")

package backend

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class BackendConfiguration {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            SpringApplication.run(BackendConfiguration::class.java, *args)
        }
    }
}