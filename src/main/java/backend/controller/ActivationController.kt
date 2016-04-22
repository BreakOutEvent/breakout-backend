package backend.controller

import backend.controller.exceptions.NotFoundException
import backend.model.misc.EmailAddress
import backend.model.user.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod.GET
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/activation")
class ActivationController {

    private val userService: UserService

    @Autowired
    constructor(userService: UserService) {
        this.userService = userService
    }

    @RequestMapping(method = arrayOf(GET))
    fun activateAccount(@RequestParam token: String,
                        @RequestParam email: EmailAddress) {

        val user = userService.getUserByEmail(email.toString()) ?:
                throw NotFoundException("No user with email $email")

        userService.activate(user, token)
    }
}
