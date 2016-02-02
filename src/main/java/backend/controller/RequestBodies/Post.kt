@file:JvmName("Post")
package backend.controller.RequestBodies

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotNull

class Post {
    @NotNull var created: Date? = null
    @NotNull var sent: Date? = null
    @NotNull var text: String? = null
    @NotNull @Valid var location: Location? = null
    @NotNull @JsonProperty("challenge_id") var challengeId: String? = null
}
