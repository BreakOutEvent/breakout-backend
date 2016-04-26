package backend.model.payment

import backend.model.user.User
import org.javamoney.moneta.Money

interface TeamEntryFeeService {
    fun findById(id: Long): TeamEntryFeeInvoice?
    fun save(invoice: TeamEntryFeeInvoice)
    fun addAdminPaymentToInvoice(user: User, amount: Money, invoice: TeamEntryFeeInvoice): Invoice
}
