package backend.view

import backend.model.messaging.Message
import backend.view.user.BasicUserView
import org.hibernate.validator.constraints.SafeHtml
import org.hibernate.validator.constraints.SafeHtml.WhiteListType.NONE
import java.time.ZoneOffset
import javax.validation.constraints.NotNull

class MessageView() {

    var id: Long? = null

    lateinit var creator: BasicUserView

    @NotNull
    @SafeHtml(whitelistType = NONE)
    var text: String? = null

    @NotNull
    var date: Long? = null

    constructor(message: Message) : this() {
        this.id = message.id
        this.date = message.date.toEpochSecond(ZoneOffset.UTC)
        this.text = message.text
        this.creator = BasicUserView(message.creator)
    }
}
