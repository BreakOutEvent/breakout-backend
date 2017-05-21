package backend.controller.exceptions

import backend.model.payment.SponsoringInvoiceService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import kotlin.system.measureTimeMillis

@RestController
@RequestMapping("/sponsoringinvoice")
class SponsoringInvoiceController(private val sponsoringInvoiceService: SponsoringInvoiceService) {

    private val logger = LoggerFactory.getLogger(SponsoringInvoiceController::class.java)

    @PostMapping("/create/")
    fun test(@RequestParam("eventId") eventId: Long): ResponseEntity<Any> {

        logger.info("Received request to create SponsoringInvoices for event $eventId")
        var count = 0
        val time = measureTimeMillis {
            count = sponsoringInvoiceService.createInvoicesForEvent(eventId)
        }
        logger.info("Created $count invoices in $time ms")

        return ResponseEntity.created(URI.create("/sponsoringinvoice/")).body(mapOf("message" to "created"))
    }
}
