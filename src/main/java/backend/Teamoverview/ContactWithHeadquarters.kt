package backend.teamoverview

import backend.model.BasicEntity
import backend.model.event.Team
import backend.model.user.UserAccount
import javax.persistence.*

@Entity
class ContactWithHeadquarters : BasicEntity {

    @Column(columnDefinition = "TEXT")
    var comment: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    var admin: UserAccount? = null

    @ManyToOne(fetch = FetchType.LAZY)
    var team: Team? = null

    private constructor() : super()

    constructor(team: Team, comment: String, admin: UserAccount) {
        this.team = team
        this.comment = comment
        this.admin = admin
    }

}
