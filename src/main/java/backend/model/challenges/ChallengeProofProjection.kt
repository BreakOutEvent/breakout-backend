package backend.model.challenges

import org.javamoney.moneta.Money

interface ChallengeProofProjection {
    fun getId(): Long
    fun getStatus(): String
    fun getAmount(): Money
    fun getDescription(): String
}
