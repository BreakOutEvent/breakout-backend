package backend.model.payment

import backend.model.user.Admin
import org.javamoney.moneta.Money

interface TeamEntryFeeService {

    fun findById(id: Long): TeamEntryFeeInvoice?

    fun findByPurposeOfTransferCode(purposeOfTransferCode:String): TeamEntryFeeInvoice?

    fun save(invoice: TeamEntryFeeInvoice): TeamEntryFeeInvoice

    fun addAdminPaymentToInvoice(admin: Admin, amount: Money, invoice: TeamEntryFeeInvoice): TeamEntryFeeInvoice

    fun  addSepaPaymentToInvoice(admin: Admin, fidorId: Long,amount: Money, invoice: TeamEntryFeeInvoice): TeamEntryFeeInvoice

}
