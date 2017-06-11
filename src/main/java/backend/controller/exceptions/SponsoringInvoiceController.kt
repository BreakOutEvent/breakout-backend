package backend.controller.exceptions

import backend.model.event.EventService
import backend.model.payment.SponsoringInvoiceService
import backend.view.sponsoring.SponsoringInvoiceView
import org.slf4j.LoggerFactory
import org.springframework.data.repository.query.Param
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.net.URI
import kotlin.system.measureTimeMillis

@RestController
@RequestMapping("/sponsoringinvoice")
class SponsoringInvoiceController(private val sponsoringInvoiceService: SponsoringInvoiceService,
                                  private val eventService: EventService) {

    private val logger = LoggerFactory.getLogger(SponsoringInvoiceController::class.java)

    @PostMapping("/create/")
    @PreAuthorize("hasAuthority('ADMIN')")
    fun test(@RequestParam("eventId") eventId: Long): ResponseEntity<Any> {

        val event = eventService.findById(eventId) ?: throw NotFoundException("event $eventId not found")
        logger.info("Received request to create SponsoringInvoices for event ${event.id}")
        var count = 0
        val time = measureTimeMillis {
            count = sponsoringInvoiceService.createInvoicesForEvent(event)
        }
        logger.info("Created $count invoices in $time ms")

        return ResponseEntity.created(URI.create("/sponsoringinvoice/")).body(mapOf("message" to "created"))
    }

    @PostMapping("/sendmails")
    @PreAuthorize("hasAuthority('ADMIN')")
            // TODO: Why does @Param work here?
    fun sendInvoiceEmailsToSponsors(@Param("eventId") eventId: Long): ResponseEntity<Any> {

        val event = eventService.findById(eventId) ?: throw NotFoundException("event $eventId not found")
        logger.info("Received request to send sponsoring invoice emails for event $eventId")

        sponsoringInvoiceService.sendInvoiceEmailsToSponsorsForEvent(event)

        return ok(mapOf("message" to "ok"))
    }

    /**
     * POST /sponsoringinvoice/sendsponsorreminder/?eventId=XYZ
     *
     * Send an email reminder to all sponsors at event EVENT_ID that have not yet fully paid
     * their sponsoring invoice
     */
    @PostMapping("/sendsponsorreminder/")
    @PreAuthorize("hasAuthority('ADMIN')")
    fun sendInvoiceEmailsToSponsorsWhereEmailUnpaid(@RequestParam eventId: Long): ResponseEntity<Any> {
        val event = eventService.findById(eventId) ?: throw NotFoundException("event $eventId not found")
        logger.info("Received request to send sponsoring invoice emails for event $eventId where sponsors have not yet fully paid")
        sponsoringInvoiceService.sendInvoiceReminderEmailsToSponsorsForEvent(event)
        return ok(mapOf("message" to "ok"))
    }

    /**
     * POST /sponsoringinvoice/sendteamsponsorreminder/
     *
     * Send an email to all teams where any of their sponsors still has an open invoice
     *
     */
    @PostMapping("/sendteamsponsorreminder/")
    @PreAuthorize("hasAuthority('ADMIN')")
    fun sendSponsoringReminderEmailsToTeam(@RequestParam eventId: Long): ResponseEntity<Any> {
        val event = eventService.findById(eventId) ?: throw NotFoundException("event $eventId not found")
        logger.info("Received request to send sponsoring invoice reminders to teams where sponsor has not yet fully paid")
        sponsoringInvoiceService.sendInvoiceReminderEmailsToTeamsForEvent(event)
        return ok(mapOf("message" to "ok"))
    }

    @GetMapping("/{eventId}/")
    @PreAuthorize("hasAuthority('ADMIN')")
    fun getInvoicesByEvent(@PathVariable("eventId") eventId: Long): Iterable<SponsoringInvoiceView> {

        eventService.findById(eventId) ?: throw NotFoundException("Event with id $eventId not found")
        return sponsoringInvoiceService.findByEventId(eventId).map {
            SponsoringInvoiceView(it)
        }
    }
}
