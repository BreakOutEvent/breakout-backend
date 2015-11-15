package backend.controller;

import backend.model.Greeting;
import backend.repository.GreetingRepository;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import springfox.documentation.spring.web.json.Json;

import java.util.concurrent.atomic.AtomicLong;

@Controller
@RequestMapping("/helloworld")
public class HelloWorldController {

    @Autowired
    private GreetingRepository greetingRepository;

    private static final String template = "Hello and welcome to BreakOut, %s!";

    @ApiOperation(value = "helloworld", response = Greeting.class)
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object sayHello(@RequestParam(value = "name", required = false, defaultValue = "Stranger") String name) {
        Greeting greeting = new Greeting(String.format(template, name));
        greetingRepository.save(greeting);
        return greeting;
    }

    @RequestMapping(method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object deleteHello(@RequestParam(value = "id", required = true) long id) {
        Greeting greeting = greetingRepository.findOne(id);
        if (greeting != null) {
            greetingRepository.delete(greeting);
        }
        return new Json("{\"success\": \"deleted\"}");
    }
}