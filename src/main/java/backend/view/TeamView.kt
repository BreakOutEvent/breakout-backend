package backend.view

import backend.model.event.Team
import backend.model.removeBlockedBy
import backend.util.data.DonateSums
import backend.view.user.BasicUserView
import org.hibernate.validator.constraints.SafeHtml
import org.hibernate.validator.constraints.SafeHtml.WhiteListType.NONE
import java.util.*
import javax.validation.Valid

class TeamView() {

    var id: Long? = null

    @Valid
    @SafeHtml(whitelistType = NONE)
    var name: String? = null

    var event: Long? = null

    @Valid
    @SafeHtml(whitelistType = NONE)
    var description: String? = null

    var hasStarted: Boolean? = null

    var members: MutableList<BasicUserView>? = null

    var profilePic: MediaView? = null

    var invoiceId: Long? = null

    var hasFullyPaid: Boolean? = null

    var isFull: Boolean? = null

    var distance: Double? = null

    var donateSum: DonateSums? = null

    var score: Double? = null

    var asleep: Boolean? = null

    var postaddress: String? = null

    constructor(team: Team, userId: Long?) : this() {
        this.id = team.id
        this.name = team.name
        this.event = team.event.id
        this.description = team.description

        this.members = ArrayList()
        team.members.removeBlockedBy(userId).forEach { this.members!!.add(BasicUserView(it)) }

        this.profilePic = team.profilePic?.let(::MediaView)
        this.invoiceId = team.invoice?.id
        this.hasStarted = team.hasStarted
        this.hasFullyPaid = team.invoice?.isFullyPaid() ?: true
        this.isFull = team.isFull()
        this.asleep = team.asleep
        this.postaddress = team.postaddress
    }

    constructor(team: Team, distance: Double, donateSum: DonateSums, score: Double, userId: Long?) : this() {
        this.id = team.id
        this.name = team.name
        this.event = team.event.id
        this.description = team.description

        this.members = ArrayList()
        team.members.removeBlockedBy(userId).forEach { this.members!!.add(BasicUserView(it)) }

        this.profilePic = team.profilePic?.let(::MediaView)
        this.invoiceId = team.invoice?.id
        this.hasStarted = team.hasStarted
        this.hasFullyPaid = team.invoice?.isFullyPaid() ?: true
        this.isFull = team.isFull()
        this.distance = distance
        this.donateSum = donateSum
        this.score = score
        this.asleep = team.asleep
        this.postaddress = team.postaddress
    }
}
