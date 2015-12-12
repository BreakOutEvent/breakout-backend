package backend.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

@Controller
class StaticController {
    @RequestMapping(value = "/", method = arrayOf(RequestMethod.GET))
    fun apiDoc(): String {
        return "redirect:swagger-ui.html"
    }
}
