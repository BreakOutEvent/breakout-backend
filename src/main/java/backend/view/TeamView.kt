package backend.view

import backend.model.event.Team
import org.hibernate.validator.constraints.NotEmpty
import org.springframework.hateoas.ResourceSupport
import java.util.*
import javax.validation.constraints.NotNull
import kotlin.collections.forEach

class TeamView : ResourceSupport {

    var id: Long? = null

    @NotEmpty
    var name: String? = null

    @NotNull
    var event: Long? = null

    @NotNull
    var description: String? = null

    var members: MutableList<String>? = null

    constructor(team: Team) : super() {
        this.id = team.id
        this.name = team.name
        this.event = team.event.id
        this.description = team.description
        this.members = ArrayList<String>()
        team.members.forEach { this.members!!.add(it.email) }
    }
}
