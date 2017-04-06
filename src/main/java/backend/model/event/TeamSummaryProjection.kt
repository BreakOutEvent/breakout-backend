@file:JvmName("TeamSummaryProjection")

package backend.model.event


class TeamSummaryProjection(val teamName: String, val teamId: Long, val eventId: Long, val eventTitle: String)
