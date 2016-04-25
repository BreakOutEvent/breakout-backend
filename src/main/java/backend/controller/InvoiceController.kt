package backend.controller

import backend.model.payment.TeamEntryFeeService
import backend.model.user.UserService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/invoice")
open class InvoiceController {

    private val teamEntryFeeService: TeamEntryFeeService
    private val userService: UserService
    private val logger: Logger

    @Autowired
    constructor(teamEntryFeeService: TeamEntryFeeService, userService: UserService) {
        this.teamEntryFeeService = teamEntryFeeService
        this.userService = userService
        this.logger = Logger.getLogger(InvoiceController::class.java)
    }
}
