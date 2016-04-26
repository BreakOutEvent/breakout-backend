package backend.model.payment

import backend.model.user.Admin
import org.javamoney.moneta.Money

interface TeamEntryFeeService {
    fun findById(id: Long): TeamEntryFeeInvoice?
    fun save(invoice: TeamEntryFeeInvoice): TeamEntryFeeInvoice
    fun addAdminPaymentToInvoice(admin: Admin, amount: Money, invoice: TeamEntryFeeInvoice): TeamEntryFeeInvoice
}
