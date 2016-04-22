package backend.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import springfox.documentation.annotations.ApiIgnore

// TODO: Remove in production

@Controller
class StaticController {

    @ApiIgnore
    @RequestMapping("/")
    fun apiDoc(): String {
        return "redirect:swagger-ui.html"
    }
}
