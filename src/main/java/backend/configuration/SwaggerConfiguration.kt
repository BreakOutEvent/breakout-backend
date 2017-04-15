@file:JvmName("SwaggerConfiguration")

package backend.configuration

import backend.util.Profiles.DEVELOPMENT
import backend.util.Profiles.STAGING
import com.google.common.base.Predicates
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors.regex
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.Contact
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger.web.UiConfiguration
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
@Profile(DEVELOPMENT, STAGING)
@EnableSwagger2
class SwaggerConfiguration {

    @Bean
    fun api() = Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.any())
            .apis(Predicates.not(RequestHandlerSelectors.basePackage("org.springframework.boot")))
            .paths(regex("/.*"))
            .build()
            .apiInfo(apiInfo())

    @Bean
    fun uiConfiguration() = UiConfiguration.DEFAULT

    private fun apiInfo(): ApiInfo? {
        val florian = Contact("Florian Schmidt", "break-out.org", "florian.schmidt@break-out.org")
        val philipp = Contact("Philipp Piwowarsky", "break-out.org", "philipp.piwowarsky@break-out.org")

        return ApiInfoBuilder()
                .title("Breakout Backend REST API")
                .description("This is the description of the REST API for the Breakout Backend")
                .version("1.1.0")
                .contact(florian)
                .contact(philipp)
                .license("GNU AGPL v3")
                .licenseUrl("https://www.gnu.org/licenses/agpl-3.0.de.html")
                .build()
    }
}
