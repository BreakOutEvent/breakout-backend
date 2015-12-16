package backend

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity

@Configuration
@EnableWebMvcSecurity
open class WebSecurityConfig : WebSecurityConfigurerAdapter() {

    // Add default security but for now allow all requests
    override fun configure(http: HttpSecurity) {
        http.authorizeRequests()
                .antMatchers("*").permitAll()
    }
}