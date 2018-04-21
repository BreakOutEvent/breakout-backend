package backend.view.challenge

import backend.converter.MoneySerializer
import backend.view.SponsorTeamProfileView
import com.fasterxml.jackson.databind.annotation.JsonSerialize

class ChallengeTeamProfileView(
        val id: Long?,
        @JsonSerialize(using = MoneySerializer::class)
        val amount: org.javamoney.moneta.Money,
        val description: String,
        val status: String,
        val sponsor: SponsorTeamProfileView?)