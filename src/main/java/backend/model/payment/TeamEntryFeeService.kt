package backend.model.payment

interface TeamEntryFeeService {
    fun findById(id: Long): Invoice?
    fun save(invoice: TeamEntryFeeInvoice)
    fun addPaymentToInvoice(invoice: Invoice, payment: Payment)
}
