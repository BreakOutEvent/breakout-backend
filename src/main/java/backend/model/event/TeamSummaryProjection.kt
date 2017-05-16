package backend.model.event

import org.springframework.beans.factory.annotation.Value

interface TeamSummaryProjection {
    @Value("#{target.id}")
    fun getTeamId(): Long

    @Value("#{target.name}")
    fun getTeamName(): String

    @Value("#{target.event.id}")
    fun getEventId(): Long

    @Value("#{target.event.title}")
    fun getEventTitle(): String
}