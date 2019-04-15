package backend.teamoverview

import backend.model.BasicEntity
import backend.model.event.Team
import backend.model.user.UserAccount
import javax.persistence.*

@Entity
class ContactWithHeadquarters : BasicEntity {

    enum class Reason {
        TECHNICAL_PROBLEM,
        FIVE_HOUR_NOTIFICATION,
        NEW_TRANSPORT,
        FINISHED,
        SICKNESS,
        EMERGENCY,
        OTHER
    }

    @ManyToOne(fetch = FetchType.LAZY)
    var team: Team? = null

    var reason: Reason? = null

    @Column(columnDefinition = "TEXT")
    var comment: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    var admin: UserAccount? = null

    private constructor() : super()

    constructor(team: Team, reason: Reason, comment: String?, admin: UserAccount) {
        this.team = team
        this.reason = reason
        this.comment = comment
        this.admin = admin
    }

}
