package backend.configuration

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import org.springframework.web.client.RestTemplate
import org.springframework.web.filter.AbstractRequestLoggingFilter
import java.io.IOException
import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@EnableScheduling
@Configuration
class GeneralConfiguration {

    @Bean
    fun restOperations(): RestOperations {
        return RestTemplate()
    }

    @Bean
    fun requestLoggingFilter(): CustomLoggingFilter {
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

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class SimpleCORSFilter : Filter {

    private val log = LoggerFactory.getLogger(SimpleCORSFilter::class.java)

    init {
        log.info("SimpleCORSFilter init")
    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {

        val response = res as HttpServletResponse
        val request = req as HttpServletRequest

        response.setHeader("Access-Control-Allow-Origin", "*")
        response.setHeader("Access-Control-Allow-Credentials", "true")
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT")
        response.setHeader("Access-Control-Max-Age", "3600")
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With, remember-me, Authorization")

        if (req.method != "OPTIONS") {
            chain.doFilter(req, res)
        } else {
            // break filter chain
        }
    }

    override fun init(filterConfig: FilterConfig) {}

    override fun destroy() {}

}
