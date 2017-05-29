package backend.controller.exceptions

import backend.model.event.EventService
import backend.model.payment.SponsoringInvoiceService
import org.omg.CosNaming.NamingContextPackage.NotFound
import org.slf4j.LoggerFactory
import org.springframework.data.repository.query.Param
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import kotlin.system.measureTimeMillis

@RestController
@RequestMapping("/sponsoringinvoice")
class SponsoringInvoiceController(private val sponsoringInvoiceService: SponsoringInvoiceService,
                                  private val eventService: EventService) {

    private val logger = LoggerFactory.getLogger(SponsoringInvoiceController::class.java)

    @PostMapping("/create/")
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

    @PostMapping("/sendemails")
    fun sendInvoiceEmailsToSponsors(@Param("eventId") eventId: Long): ResponseEntity<Any> {

        val event = eventService.findById(eventId) ?: throw NotFoundException("event $eventId not found")
        logger.info("Received request to send sponsoring invoice emails for event $eventId")

        sponsoringInvoiceService.sendInvoiceEmailsToSponsorsForEvent(event)

        return ok(mapOf("message" to "ok"))
    }
}
