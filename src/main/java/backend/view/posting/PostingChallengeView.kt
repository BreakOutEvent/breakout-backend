package backend.view.posting

import backend.model.challenges.ChallengeProofProjection

class PostingChallengeView {

    var id: Long? = null
    var status: String? = null
    var amount: Double? = null
    var description: String? = null

    /**
     * no-args constructor for Jackson
     */
    constructor()

    constructor(challenge: ChallengeProofProjection) {
        this.id = challenge.getId()
        this.status = challenge.getStatus()
        this.amount = challenge.getAmount().numberStripped.toDouble()
        this.description = challenge.getDescription()
    }
}