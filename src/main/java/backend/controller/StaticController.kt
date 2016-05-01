package backend.controller

import backend.util.Profiles
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod.GET
import springfox.documentation.annotations.ApiIgnore

@Profile(Profiles.DEVELOPMENT)
@Controller
class StaticController {

    @ApiIgnore
    @RequestMapping("/", method = arrayOf(GET))
    fun apiDoc(): String {
        return "redirect:swagger-ui.html"
    }
}
