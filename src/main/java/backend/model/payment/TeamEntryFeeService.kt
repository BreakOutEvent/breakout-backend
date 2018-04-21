package backend.model.payment

import backend.model.user.Admin
import org.javamoney.moneta.Money
import java.time.LocalDateTime

interface TeamEntryFeeService {

    fun findById(id: Long): TeamEntryFeeInvoice?

    fun findByPurposeOfTransferCode(purposeOfTransferCode: String): TeamEntryFeeInvoice?

    fun save(invoice: TeamEntryFeeInvoice): TeamEntryFeeInvoice

    fun addAdminPaymentToInvoice(admin: Admin, amount: Money, invoice: TeamEntryFeeInvoice): TeamEntryFeeInvoice

    fun addSepaPaymentToInvoice(admin: Admin, fidorId: Long, amount: Money, date: LocalDateTime?, invoice: TeamEntryFeeInvoice): TeamEntryFeeInvoice

    fun findAll(): Iterable<TeamEntryFeeInvoice>

}
