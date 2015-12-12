@file:JvmName("SwaggerConfiguration")

package backend

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors.regex
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger.web.UiConfiguration
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
@EnableSwagger2
open class SwaggerConfiguration {

    @Bean
    open fun api() = Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.any())
            .paths(regex("/.*"))
            .build()
            .apiInfo(apiInfo())

    @Bean
    open fun uiConfiguration() = UiConfiguration.DEFAULT

    private fun apiInfo() = ApiInfoBuilder()
            .title("Breakout Backend REST API")
            .description("This is the description of the REST API for the Breakout Backend")
            .version("0.0.1").contact("florian.schmidt@break-out.org")
            .license("Add some license here")
            .termsOfServiceUrl("Add url to terms of service here")
            .build()
}