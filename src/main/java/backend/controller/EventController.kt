package backend.controller

import backend.controller.exceptions.NotFoundException
import backend.exceptions.CacheNonExistentException
import backend.model.cache.CacheService
import backend.model.event.EventService
import backend.model.misc.Coord
import backend.util.CacheNames.LOCATIONS
import backend.view.EventView
import backend.view.WhitelistDomainView
import backend.view.WhitelistEmailView
import org.javamoney.moneta.Money
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.http.HttpStatus.CREATED
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import javax.validation.Valid

@RestController
@RequestMapping("/event")
class EventController(open var eventService: EventService,
                      open var cacheService: CacheService) {

    private var logger: Logger = LoggerFactory.getLogger(EventController::class.java)

    /**
     * POST /event/
     * Allows admin to create new event
     */
    @PreAuthorize("hasAuthority('EVENT_OWNER')")
    @ResponseStatus(CREATED)
    @PostMapping("/")
    @CacheEvict(LOCATIONS, allEntries = true)
    fun createEvent(@Valid @RequestBody body: EventView): EventView {

        val teamFee = body.teamFee ?: 60.0
        val brand = body.brand ?: "BreakOut ${Date().year}"
        val bank = body.bank ?: "Fidor Bank"
        val iban = body.iban ?: "DE85 7002 2200 0020 2418 37"
        val bic = body.bic ?: "FDDODEMMXXX"

        val event = eventService.createEvent(
                title = body.title,
                date = LocalDateTime.ofEpochSecond(body.date!!, 0, ZoneOffset.UTC),
                city = body.city,
                duration = body.duration,
                startingLocation = Coord(body.startingLocation.latitude!!, body.startingLocation.longitude!!),
                teamFee = Money.of(teamFee, "EUR"),
                brand = brand,
                bank = bank,
                iban = iban,
                bic = bic
        )

        return EventView(event)
    }

    /**
     * PUT /event/{id}/
     * Allows admin to update event details
     */
    @PreAuthorize("hasAuthority('EVENT_OWNER')")
    @ResponseStatus(CREATED)
    @PutMapping("/{id}/")
    @CacheEvict(LOCATIONS, allEntries = true)
    fun updateEvent(@PathVariable("id") id: Long, @Valid @RequestBody body: EventView): EventView {
        val event = eventService.findById(id) ?: throw NotFoundException("event with id $id does not exist")

        event.title = body.title
        event.date = body.date?.let { LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC) } ?: event.date
        event.city = body.city
        event.duration = body.duration
        event.startingLocation = Coord(body.startingLocation.latitude!!, body.startingLocation.longitude!!)
        event.teamFee = body.teamFee?.let { Money.of(it, "EUR") } ?: event.teamFee
        event.brand = body.brand ?: event.brand
        event.bank = body.bank ?: event.bank
        event.iban = body.iban ?: event.iban
        event.bic = body.bic ?: event.bic

        event.isCurrent = body.isCurrent
        event.isOpenForRegistration = body.isOpenForRegistration
        event.allowNewSponsoring = body.allowNewSponsoring

        eventService.save(event)

        return EventView(event)
    }

    /**
     * POST /event/{id}/whitelistMail/
     * Allows admin to create new event
     */
    @PreAuthorize("hasAuthority('EVENT_OWNER')")
    @ResponseStatus(CREATED)
    @PostMapping("/{id}/whitelistMail/")
    fun addEmailWhitelist(@PathVariable("id") id: Long, @Valid @RequestBody body: WhitelistEmailView): WhitelistEmailView? {
        val event = eventService.findById(id) ?: throw NotFoundException("event with id $id does not exist")
        val whitelistEntry = eventService.addEmailToWhitelist(event, body.email)
        return whitelistEntry?.let { WhitelistEmailView(it) }

    }

    /**
     * POST /event/{id}/whitelistDomain/
     * Allows admin to create new event
     */
    @PreAuthorize("hasAuthority('EVENT_OWNER')")
    @ResponseStatus(CREATED)
    @PostMapping("/{id}/whitelistDomain/")
    fun addDomainWhitelist(@PathVariable("id") id: Long, @Valid @RequestBody body: WhitelistDomainView): WhitelistDomainView? {
        val event = eventService.findById(id) ?: throw NotFoundException("event with id $id does not exist")
        val whitelistEntry = eventService.addDomainToWhitelist(event, body.domain)
        return whitelistEntry?.let { WhitelistDomainView(it) }
    }

    /**
     * GET /event/
     * Gets a list of all events
     */
    @GetMapping("/")
    fun getAllEvents(): Iterable<EventView> {
        return eventService.findAll().map(::EventView)
    }

    /**
     * GET /event/{id}/
     * Gets Event by ID
     */
    @GetMapping("/{id}/")
    fun getEventById(@PathVariable("id") id: Long): EventView {
        val event = eventService.findById(id) ?: throw NotFoundException("event with id $id does not exist")
        return EventView(event)
    }

    /**
     * GET /event/{id}/posting/
     * Gets all Postings for given event
     */
    @GetMapping("/{id}/posting/")
    fun getEventPostings(@PathVariable("id") id: Long): List<Long> {
        return eventService.findPostingsById(id) ?: throw NotFoundException("event with id $id does not exist")
    }

    /**
     * GET /event/{id}/distance/
     * Returns the sum of the distance of all teams of the event with {id}
     */
    @GetMapping("/{id}/distance/")
    open fun getEventDistance(@PathVariable("id") id: Long): Any {
        try {
            return cacheService.getCache("Event_${id}_Distance")
        } catch (e: CacheNonExistentException) {
            eventService.regenerateCache(id)
            throw e
        }
    }

    /**
     * GET /event/{id}/donatesum/
     * Returns the sum of the distance of all teams of the event with {id}
     */
    @GetMapping("/{id}/donatesum/")
    open fun getEventDonateSum(@PathVariable("id") id: Long): Any {
        try {
            return cacheService.getCache("Event_${id}_DonateSum")
        } catch (e: CacheNonExistentException) {
            eventService.regenerateCache(id)
            throw e
        }
    }

    /*
     * GET /event/{id}/highscore/
     * Calculates and returns the team ranking of the event with {id}
     */
    @GetMapping("/{id}/highscore/")
    open fun getEventHighscore(@PathVariable("id") id: Long): Any {
        try {
            return cacheService.getCache("Event_${id}_HighScore")
        } catch (e: CacheNonExistentException) {
            eventService.regenerateCache(id)
            throw e
        }
    }
}
