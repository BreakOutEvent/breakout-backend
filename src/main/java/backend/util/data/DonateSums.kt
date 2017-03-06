package backend.util.data

import java.math.BigDecimal

data class DonateSums(val sponsorSum: BigDecimal, val withProofSum: BigDecimal, val acceptedProofSum: BigDecimal, val fullSum: BigDecimal)

data class ChallengeDonateSums(val withProofSum: BigDecimal, val acceptedProofSum: BigDecimal)
