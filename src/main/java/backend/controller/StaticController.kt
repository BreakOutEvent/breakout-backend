package backend.controller

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import springfox.documentation.annotations.ApiIgnore

@Profile("development")
@Controller
class StaticController {

    @ApiIgnore
    @RequestMapping("/")
    fun apiDoc(): String {
        return "redirect:swagger-ui.html"
    }
}
