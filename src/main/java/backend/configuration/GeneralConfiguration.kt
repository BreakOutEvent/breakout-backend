package backend.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestOperations
import org.springframework.web.client.RestTemplate
import org.springframework.web.filter.AbstractRequestLoggingFilter
import javax.servlet.http.HttpServletRequest

@Configuration
open class GeneralConfiguration {

    @Bean
    open fun restOperations(): RestOperations {
        return RestTemplate()
    }

    @Bean
    open fun requestLoggingFilter(): CustomLoggingFilter {
        val crlf = CustomLoggingFilter()
        crlf.setIncludeQueryString(true)
        crlf.setIncludePayload(false)
        return crlf
    }
}


class CustomLoggingFilter : AbstractRequestLoggingFilter() {

    override fun shouldLog(request: HttpServletRequest?): Boolean {
        return logger.isDebugEnabled
    }

    override fun beforeRequest(request: HttpServletRequest, message: String) {
        request.setAttribute("timing", System.currentTimeMillis())
    }

    override fun afterRequest(request: HttpServletRequest, message: String) {
        logger.debug("${System.currentTimeMillis() - request.getAttribute("timing") as Long}ms ${request.method} ${request.requestURI} - ${request.getHeader("User-Agent")}")
    }

}

