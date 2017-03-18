package backend.model.event

import backend.model.location.Location
import backend.model.misc.Coord
import backend.util.data.DonateSums
import java.time.LocalDateTime

interface EventService {
    fun findById(id: Long): Event?

    fun createEvent(title: String, date: LocalDateTime, city: String, startingLocation: Coord, duration: Int): Event

    fun findAll(): Iterable<Event>

    fun exists(id: Long): Boolean

    /**
     * Get Event Postings from database
     *
     * @param id: The id of Event to get postings for
     *
     * @return gotten postings list
     */
    fun findPostingsById(id: Long): List<Long>?

    /**
     * Get Event Postings from database only including location
     *
     * @param id: The id of Event to get postings for
     *
     * @return gotten postings list
     */
    fun findLocationPostingsById(id: Long): List<Location>

    fun getLocationMaxDistanceByIdEachTeam(id: Long): List<Location>

    fun getDistance(id: Long): Double

    fun getDonateSum(id: Long): DonateSums

    fun regenerateCache()
}
