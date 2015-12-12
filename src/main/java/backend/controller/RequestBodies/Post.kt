@file:JvmName("Post")
package backend.controller.RequestBodies

import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.Valid
import javax.validation.constraints.NotNull
import java.util.Date

class Post {
    @NotNull var created: Date? = null
    @NotNull var sent: Date? = null
    @NotNull var text: String? = null
    @NotNull @Valid var location: Location? = null
    @NotNull @JsonProperty("challenge_id") var challengeId: String? = null
}
