package backend.controller

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.NotFoundException
import backend.controller.exceptions.UnauthorizedException
import backend.model.messaging.GroupMessageService
import backend.model.messaging.Message
import backend.model.user.UserService
import backend.util.localDateTimeOf
import backend.view.GroupMessageView
import backend.view.MessageView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.CREATED
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestMethod.*
import javax.validation.Valid

@RestController
@RequestMapping("/messaging")
open class MessagingController {

    private val groupMessageService: GroupMessageService
    private val userService: UserService

    @Autowired
    constructor(groupMessageService: GroupMessageService, userService: UserService) {
        this.groupMessageService = groupMessageService
        this.userService = userService
    }


    /**
     * POST /messaging/
     * creates new GroupMessage
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/", method = arrayOf(POST))
    @ResponseStatus(CREATED)
    open fun createGroupMessage(@Valid @RequestBody body: List<Long>,
                                @AuthenticationPrincipal customUserDetails: CustomUserDetails): GroupMessageView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val groupMessage = groupMessageService.createGroupMessage(user.core)

        body.forEach { userId ->
            val userToAdd = userService.getUserById(userId) ?: throw NotFoundException("user with id $userId does not exist")
            if (!groupMessage.users.contains(userToAdd.core)) groupMessageService.addUser(userToAdd.core, groupMessage)
        }

        return GroupMessageView(groupMessage)
    }

    /**
     * PUT /messaging/{id}/
     * creates new GroupMessage
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/{id}/", method = arrayOf(PUT))
    open fun editGroupMessage(@PathVariable("id") id: Long,
                              @Valid @RequestBody body: List<Long>,
                              @AuthenticationPrincipal customUserDetails: CustomUserDetails): GroupMessageView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val groupMessage = groupMessageService.getByID(id) ?: throw NotFoundException("groupmessage with id $id does not exist")
        if (!groupMessage.users.contains(user.core)) throw UnauthorizedException("authenticated user and requested resource mismatch")

        body.forEach { userId ->
            val userToAdd = userService.getUserById(userId) ?: throw NotFoundException("user with id $userId does not exist")
            if (!groupMessage.users.contains(userToAdd.core)) groupMessageService.addUser(userToAdd.core, groupMessage)
        }

        return GroupMessageView(groupMessage)
    }

    /**
     * POST /messaging/{id}/message/
     * creates new Message for GroupMessage
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/{id}/message/", method = arrayOf(POST))
    @ResponseStatus(CREATED)
    open fun addMessage(@PathVariable("id") id: Long,
                        @Valid @RequestBody body: MessageView,
                        @AuthenticationPrincipal customUserDetails: CustomUserDetails): GroupMessageView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val groupMessage = groupMessageService.getByID(id) ?: throw NotFoundException("groupmessage with id $id does not exist")
        if (!groupMessage.users.contains(user.core)) throw UnauthorizedException("authenticated user and requested resource mismatch")

        val message = Message(user.core, body.text!!, localDateTimeOf(body.date!!))
        groupMessageService.addMessage(message, groupMessage)

        return GroupMessageView(groupMessage)
    }

    /**
     * GET /messaging/{id}/
     * gets a GroupMessage
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/{id}/", method = arrayOf(GET))
    open fun getGroupMessage(@PathVariable("id") id: Long,
                             @AuthenticationPrincipal customUserDetails: CustomUserDetails): GroupMessageView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val groupMessage = groupMessageService.getByID(id) ?: throw NotFoundException("groupmessage with id $id does not exist")
        if (!groupMessage.users.contains(user.core)) throw UnauthorizedException("authenticated user and requested resource mismatch")

        return GroupMessageView(groupMessage)
    }
}
