package backend.controller

import backend.controller.exceptions.NotFoundException
import backend.model.user.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/activation")
class ActivationController(private val userService: UserService) {

    /**
     * GET /activation?={token}
     * Activates account with given token
     */
    @GetMapping
    fun activateAccount(@RequestParam token: String): Map<String, String> {

        val user = userService.getUserByActivationToken(token) ?:
                throw NotFoundException("No user with token $token")

        userService.activate(user, token)
        return mapOf("message" to "success")
    }
}
