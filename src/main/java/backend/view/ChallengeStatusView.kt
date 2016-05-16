package backend.view

import javax.validation.constraints.NotNull

class ChallengeStatusView {
    @NotNull var status: String? = null
    var postingId: Long? = null
}
