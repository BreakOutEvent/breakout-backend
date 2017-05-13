package backend.view

import backend.model.messaging.GroupMessage
import backend.model.user.UserAccount
import org.codehaus.jackson.annotate.JsonProperty;

class NotificationView {

    @JsonProperty("app_id") val appId: String
    @JsonProperty("headings") val title: String
    @JsonProperty("contents") val subtitle: String?
    @JsonProperty("include_player_ids") val tokens: List<String>
    @JsonProperty val data: GroupMessage

    constructor(appId: String, title: String, subtitle: String?, users: List<UserAccount>, data: GroupMessage) {
        this.appId = appId
        this.title = title
        this.subtitle = subtitle
        this.tokens = users.mapNotNull { it.notificationToken }
        this.data = data
    }

}