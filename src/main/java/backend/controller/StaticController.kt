package backend.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import springfox.documentation.annotations.ApiIgnore

@Controller
class StaticController {

    @ApiIgnore
    @RequestMapping(value = "/", method = arrayOf(RequestMethod.GET))
    fun apiDoc(): String {
        return "redirect:swagger-ui.html"
    }
}
