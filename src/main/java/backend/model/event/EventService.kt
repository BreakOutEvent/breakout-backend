package backend.model.event

import backend.model.location.Location
import backend.model.misc.Coord
import backend.model.user.User
import backend.util.data.DonateSums
import org.javamoney.moneta.Money
import java.time.LocalDateTime

interface EventService {
    fun findById(id: Long): Event?

    fun createEvent(title: String, date: LocalDateTime, city: String, startingLocation: Coord, duration: Int, teamFee: Money = Money.of(60, "EUR")): Event

    fun save(event: Event)

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

    fun regenerateCache(eventId: Long?)

    fun findEvensOpenForRegistration(user: User?): List<Event>

    fun addEmailToWhitelist(event: Event, email: String): WhitelistEmailEntry?

    fun addDomainToWhitelist(event: Event, domain: String): WhitelistDomainEntry?
}
