package backend.controller

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.NotFoundException
import backend.controller.exceptions.UnauthorizedException
import backend.model.messaging.GroupMessageService
import backend.model.messaging.Message
import backend.model.user.UserService
import backend.services.NotificationService
import backend.util.localDateTimeOf
import backend.view.GroupMessageView
import backend.view.MessageView
import org.springframework.http.HttpStatus.CREATED
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/messaging")
class MessagingController(private val groupMessageService: GroupMessageService,
                          private val userService: UserService) {


    /**
     * POST /messaging/
     * creates new GroupMessage
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/")
    @ResponseStatus(CREATED)
    fun createGroupMessage(@Valid @RequestBody body: List<Long>,
                           @AuthenticationPrincipal customUserDetails: CustomUserDetails): GroupMessageView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val groupMessage = groupMessageService.createGroupMessage(user.account)

        body.forEach { userId ->
            val userToAdd = userService.getUserById(userId) ?: throw NotFoundException("user with id $userId does not exist")
            if (!groupMessage.users.contains(userToAdd.account)) groupMessageService.addUser(userToAdd.account, groupMessage)
        }

        return GroupMessageView(groupMessage)
    }

    /**
     * PUT /messaging/{id}/
     * creates new GroupMessage
     */
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}/")
    fun editGroupMessage(@PathVariable("id") id: Long,
                         @Valid @RequestBody body: List<Long>,
                         @AuthenticationPrincipal customUserDetails: CustomUserDetails): GroupMessageView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val groupMessage = groupMessageService.getByID(id) ?: throw NotFoundException("groupmessage with id $id does not exist")
        if (!groupMessage.users.contains(user.account)) throw UnauthorizedException("authenticated user and requested resource mismatch")

        body.forEach { userId ->
            val userToAdd = userService.getUserById(userId) ?: throw NotFoundException("user with id $userId does not exist")
            if (!groupMessage.users.contains(userToAdd.account)) groupMessageService.addUser(userToAdd.account, groupMessage)
        }

        return GroupMessageView(groupMessage)
    }

    /**
     * POST /messaging/{id}/message/
     * creates new Message for GroupMessage
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/message/")
    @ResponseStatus(CREATED)
    fun addMessage(@PathVariable("id") id: Long,
                   @Valid @RequestBody body: MessageView,
                   @AuthenticationPrincipal customUserDetails: CustomUserDetails): GroupMessageView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val groupMessage = groupMessageService.getByID(id) ?: throw NotFoundException("groupmessage with id $id does not exist")
        if (!groupMessage.users.contains(user.account)) throw UnauthorizedException("authenticated user and requested resource mismatch")

        val message = Message(user.account, body.text!!, localDateTimeOf(body.date!!))
        groupMessageService.addMessage(message, groupMessage)

        return GroupMessageView(groupMessage)
    }

    /**
     * GET /messaging/{id}/
     * gets a GroupMessage
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/")
    fun getGroupMessage(@PathVariable("id") id: Long,
                        @AuthenticationPrincipal customUserDetails: CustomUserDetails): GroupMessageView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val groupMessage = groupMessageService.getByID(id) ?: throw NotFoundException("groupmessage with id $id does not exist")
        if (!groupMessage.users.contains(user.account)) throw UnauthorizedException("authenticated user and requested resource mismatch")

        return GroupMessageView(groupMessage)
    }
}
