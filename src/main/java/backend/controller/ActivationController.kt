package backend.controller

import backend.controller.exceptions.NotFoundException
import backend.model.user.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/activation")
class ActivationController(private val userService: UserService) {

    /**
     * POST /activation/user?={token}
     * Activates account with given token
     */
    @PostMapping("user")
    fun activateAccount(@RequestParam token: String): Map<String, String> {

        val user = userService.getUserByActivationToken(token) ?: throw NotFoundException("No user with token $token")

        userService.activate(user, token)
        return mapOf("message" to "success")
    }

    /**
     * POST /activation/email?={token}
     * Confirms email change with given token
     */
    @PostMapping("email")
    fun confirmEmail(@RequestParam token: String): Map<String, String> {

        val user = userService.getUserByChangeEmailToken(token)
                ?: throw NotFoundException("No user with change email token $token")

        userService.confirmChangeEmail(user, token)
        return mapOf("message" to "success")
    }
}
