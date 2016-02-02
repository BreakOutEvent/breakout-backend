package backend.controller

import backend.configuration.CustomUserDetails
import backend.view.UserView
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod.GET
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/me")
class AuthenticatedUserController {

    @RequestMapping("/", method = arrayOf(GET))
    fun getAuthenticatedUser(@AuthenticationPrincipal user: CustomUserDetails): UserView {
        return UserView(user)
    }
}
