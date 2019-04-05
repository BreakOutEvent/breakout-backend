package backend.Teamoverview

import backend.model.misc.Coord
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

interface TeamOverview {

    interface Participant {
        val id: Long
        val firstname: String?
        val lastname: String?

        @Value("#{target.emergencynumber}")
        fun getEmergencyPhone(): String

        @Value("#{target.phonenumber}")
        fun getContactPhone(): String
    }

    interface Location {
        val id: Long
        val coord: Coord
        val locationData: Map<String, String>

        @Value("#{@dateTimeBean.formatDateTime(target.date)}")
        fun getTimestamp(): Long
    }

    interface Posting {
        val id: Long

        @Value("#{@dateTimeBean.formatDateTime(target.date)}")
        fun getTimestamp(): Long
    }

    interface Contact {

        interface Admin {
            val id: Long
            val firstname: String?
            val lastname: String?
        }

        val id: Long
        val admin: Admin
        val comment: String?

        @Value("#{@dateTimeBean.formatDateTime(target.createdAt)}")
        fun getTimestamp(): Long
    }

    @Value("#{target.id}")
    fun getTeamId(): Long

    @Value("#{target.name}")
    fun getTeamName(): String

    @Value("#{target.event.id}")
    fun getEventId(): Long

    @Value("#{target.event.title}")
    fun getEventTitle(): String

    @Value("#{target.members}")
    fun getMembers(): List<Participant>

    @Value("#{@locationRepository.findLastLocationByTeamId(target.id)}")
    fun getLastLocation(): Location?

    @Value("#{@postingRepository.findLastPostingByTeamId(target.id)}")
    fun getLastPosting(): Posting?

    @Value("#{@contactWithHeadquartersRepository.findLastContactByTeamId(target.id)}")
    fun getLastContactWithHeadquarters(): Contact?
}

@Component
class DateTimeBean {
    fun formatDateTime(date: LocalDateTime): Long {
        val zoneId = ZoneId.systemDefault()
        return date.atZone(zoneId).toInstant().toEpochMilli()
    }
}