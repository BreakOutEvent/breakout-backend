package backend.controller

import backend.configuration.CustomUserDetails
import backend.model.user.UserService
import backend.view.UserView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod.GET
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/me")
class AuthenticatedUserController {


    private val userService: UserService

    @Autowired
    constructor(userService: UserService) {
        this.userService = userService
    }

    @RequestMapping("/", method = arrayOf(GET))
    fun getAuthenticatedUser(@AuthenticationPrincipal customUserDetails: CustomUserDetails): UserView {
        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        return UserView(user)
    }
}
