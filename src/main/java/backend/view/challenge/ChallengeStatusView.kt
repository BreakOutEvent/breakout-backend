package backend.view.challenge

import javax.validation.constraints.NotNull

class ChallengeStatusView {

    @NotNull
    var status: String? = null

    var postingId: Long? = null
}
