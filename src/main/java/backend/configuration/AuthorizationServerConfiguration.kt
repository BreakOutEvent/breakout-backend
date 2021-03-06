package backend.configuration

import backend.services.ConfigurationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer
import org.springframework.security.oauth2.provider.token.DefaultTokenServices
import org.springframework.security.oauth2.provider.token.TokenStore
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore
import javax.annotation.PostConstruct
import javax.sql.DataSource

/*
 *
 * AuthorizationServerConfigurer is a convenient strategy for configuring an OAUth2 Authorization Server.
 * Beans of this type are applied to the Spring context automatically if you @EnableAuthorizationServer.
 *
 * From http://docs.spring.io/spring-security/oauth/apidocs/org/springframework/security/oauth2/config/annotation/web/configuration/AuthorizationServerConfigurer.html
 */

@Configuration
@EnableAuthorizationServer
class AuthorizationServerConfiguration : AuthorizationServerConfigurerAdapter() {

    @Qualifier("authenticationManagerBean")
    @Autowired lateinit private var authenticationManager: AuthenticationManager
    @Autowired lateinit private var userDetailsService: CustomUserDetailsService
    @Autowired lateinit private var configurationService: ConfigurationService
    @Autowired lateinit private var dataSource: DataSource

    private lateinit var clientName: String
    private lateinit var clientSecret: String

    @Bean
    fun tokenStore(): TokenStore {
        return JdbcTokenStore(dataSource)
    }

    @PostConstruct
    fun setUp() {
        clientName = configurationService.getRequired("org.breakout.api.client.name")
        clientSecret = configurationService.getRequired("org.breakout.api.client.secret")
    }

    @Bean
    @Primary
    fun tokenServices(): DefaultTokenServices {
        return DefaultTokenServices().apply {
            setSupportRefreshToken(true)
            setTokenStore(this@AuthorizationServerConfiguration.tokenStore())
        }
    }

    /*
     * Configure individual clients and their properties
     * Clients in this case are the corresponding breakout apps
     *
     * See: http://projects.spring.io/spring-security-oauth/docs/oauth2.html
     */
    @Throws(Exception::class)
    override fun configure(clients: ClientDetailsServiceConfigurer) {
        clients.jdbc(dataSource)
    }

    /*
     *  Configure the non-security features of the Authorization Server endpoints, like token store,
     *  token customizations, user approvals and grant types. You shouldn't need to do anything by default,
     *  unless you need password grants, in which case you need to provide an AuthenticationManager.
     */
    override fun configure(endpoints: AuthorizationServerEndpointsConfigurer) {
        endpoints
                .tokenStore(this.tokenStore())
                .authenticationManager(this.authenticationManager)
                .userDetailsService(this.userDetailsService)
    }
}
