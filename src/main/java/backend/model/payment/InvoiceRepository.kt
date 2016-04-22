package backend.model.payment

import org.springframework.data.repository.CrudRepository

interface InvoiceRepository : CrudRepository<Invoice, Long> {

}
