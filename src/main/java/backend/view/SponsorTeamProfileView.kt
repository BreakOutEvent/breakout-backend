package backend.view

import backend.converter.UrlSerializer
import backend.model.misc.Url
import com.fasterxml.jackson.databind.annotation.JsonSerialize

class SponsorTeamProfileView(
        var sponsorId: Long?,
        val firstname: String,
        val lastname: String,
        val company: String?,
        val sponsorIsHidden: Boolean,
        @JsonSerialize(using = UrlSerializer::class) val url: Url?,
        val logoUrl: String?)