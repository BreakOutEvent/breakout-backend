package backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class StaticController {
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String apiDoc() {
        return "redirect:swagger-ui.html";
    }
}
