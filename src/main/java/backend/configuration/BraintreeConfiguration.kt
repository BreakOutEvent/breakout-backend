package backend.configuration

import backend.services.ConfigurationService
import com.braintreegateway.BraintreeGateway
import com.braintreegateway.Environment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct

@Configuration
open class BraintreeConfiguration {

    private lateinit var environment: String
    private lateinit var merchantID: String
    private lateinit var publicKey: String
    private lateinit var privateKey: String

    @Autowired lateinit var configurationService: ConfigurationService

    @PostConstruct
    fun setUp() {
        this.configurationService = configurationService
        this.environment = configurationService.get("org.breakout.payment.braintree.environment") ?: "sandbox"
        this.merchantID = configurationService.getRequired("org.breakout.payment.braintree.merchantid")
        this.publicKey = configurationService.getRequired("org.breakout.payment.braintree.publickey")
        this.privateKey = configurationService.getRequired("org.breakout.payment.braintree.privatekey")
    }

    @Bean
    open fun BraintreeGateway(): BraintreeGateway {
        val env = Environment.parseEnvironment(environment)
        return com.braintreegateway.BraintreeGateway(env, merchantID, publicKey, privateKey)
    }
}
