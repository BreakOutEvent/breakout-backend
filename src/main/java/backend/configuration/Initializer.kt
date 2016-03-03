package backend.configuration

import backend.model.user.Admin
import backend.model.user.User
import backend.model.user.UserService
import backend.services.ConfigurationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class Initializer {

    private val userService: UserService
    private val adminEmail: String
    private val adminPassword: String

    @Autowired
    constructor(userService: UserService, configurationService: ConfigurationService) {
        this.userService = userService
        this.adminEmail = configurationService.getRequired("org.breakout.admin_password")
        this.adminPassword = configurationService.getRequired("org.breakout.admin_email")
    }

    @PostConstruct
    fun initialize() {
        val admin = User.create(adminPassword, adminEmail)
        admin.addRole(Admin::class)
        admin.isBlocked = false
        userService.save(admin)
    }
}
