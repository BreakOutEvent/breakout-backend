package backend.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer

/*
 * The ResourceServerConfigurerAdapter is an adapter for the interface for @EnableResourceServer classes.
 * Implement this interface to adjust the access rules and paths that are protected by OAuth2 security.
 * Applications may provide multiple instances of this interface, and in general (like with other Security configurers),
 * if more than one configures the same preoperty, then the last one wins.
 * The configurers are sorted by Order before being applied.
 *
 * From http://docs.spring.io/spring-security/oauth/apidocs/org/springframework/security/oauth2/config/annotation/web/configuration/ResourceServerConfigurer.html
 */

@Configuration
@EnableResourceServer
class ResourceServerConfiguration : ResourceServerConfigurerAdapter() {

    override fun configure(resources: ResourceServerSecurityConfigurer?) {
        resources!!.resourceId("BREAKOUT_BACKEND")
    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity?) {
        http!!.authorizeRequests()
                .antMatchers(HttpMethod.PUT, "/user/{id}/*").authenticated()
                .antMatchers(HttpMethod.GET, "/user/{id}/*").permitAll()
                .antMatchers("/oauth/*").permitAll()//TODO: Add routes which should be secured
    }
}
