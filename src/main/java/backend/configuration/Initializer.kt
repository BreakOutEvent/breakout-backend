package backend.configuration

import backend.model.user.Admin
import backend.model.user.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class Initializer {

    private val userService: UserService

    @Autowired
    constructor(userService: UserService) {
        this.userService = userService
    }

    @Value("\${org.breakout.admin_password}")
    private lateinit var ADMIN_PASSWORD: String

    @Value("\${org.breakout.admin_email}")
    private lateinit var ADMIN_EMAIL: String

    @PostConstruct
    fun initialize() {
        userService.create(ADMIN_EMAIL, ADMIN_PASSWORD).addRole(Admin::class.java)
    }
}
